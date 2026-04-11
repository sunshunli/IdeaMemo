package dev.jeziellago.compose.markdowntext

import android.content.Context
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.util.Linkify
import android.view.View
import android.widget.TextView
import androidx.annotation.FontRes
import androidx.annotation.IdRes
import androidx.compose.foundation.clickable
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.TextViewCompat
import coil.ImageLoader
import io.noties.markwon.Markwon
import java.util.regex.Matcher
import java.util.regex.Pattern

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    linkColor: Color = Color.Unspecified,
    truncateOnTextOverflow: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    isTextSelectable: Boolean = false,
    autoSizeConfig: AutoSizeConfig? = null,
    @FontRes fontResource: Int? = null,
    style: TextStyle = LocalTextStyle.current,
    @IdRes viewId: Int? = null,
    onClick: (() -> Unit)? = null,
    // this option will disable all clicks on links, inside the markdown text
    // it also enable the parent view to receive the click event
    disableLinkMovementMethod: Boolean = false,
    imageLoader: ImageLoader? = null,
    linkifyMask: Int = Linkify.EMAIL_ADDRESSES or Linkify.PHONE_NUMBERS or Linkify.WEB_URLS,
    enableSoftBreakAddsNewLine: Boolean = true,
    onLinkClicked: ((String) -> Unit)? = null,
    onTextLayout: ((numLines: Int) -> Unit)? = null,
    onTagClick: ((String) -> Unit)? = null,
) {
    val defaultColor: Color = LocalContentColor.current
    val context: Context = LocalContext.current
    val markdownRender: Markwon =
        remember {
            MarkdownRender.create(
                context,
                imageLoader,
                linkifyMask,
                enableSoftBreakAddsNewLine,
                onLinkClicked
            )
        }

    val androidViewModifier = if (onClick != null) {
        Modifier
            .clickable { onClick() }
            .then(modifier)
    } else {
        modifier
    }
    AndroidView(
        modifier = androidViewModifier,
        factory = { factoryContext ->

            val linkTextColor = linkColor.takeOrElse { style.color.takeOrElse { defaultColor } }

            CustomTextView(factoryContext).apply {
                viewId?.let { id = viewId }
                fontResource?.let { font -> applyFontResource(font) }

                setMaxLines(maxLines)
                setLinkTextColor(linkTextColor.toArgb())

                setTextIsSelectable(isTextSelectable)

                if (!isTextSelectable) {
                    movementMethod = LinkMovementMethod.getInstance()
                }

                if (truncateOnTextOverflow) enableTextOverflow()

                autoSizeConfig?.let { config ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                            this,
                            config.autoSizeMinTextSize,
                            config.autoSizeMaxTextSize,
                            config.autoSizeStepGranularity,
                            config.unit
                        )
                    }
                }
            }
        },
        update = { textView ->
            with(textView) {
                applyTextColor(style.color.takeOrElse { defaultColor }.toArgb())
                applyFontSize(style)
                applyLineHeight(style)
                applyTextDecoration(style)

                with(style) {
                    applyTextAlign(textAlign)
                    fontStyle?.let { applyFontStyle(it) }
                    fontWeight?.let { applyFontWeight(it) }
                    fontFamily?.let { applyFontFamily(it) }
                }
            }
            markdownRender.setMarkdown(textView, markdown)
            if (disableLinkMovementMethod) {
                textView.movementMethod = null
            } else if (isTextSelectable) {
                // When selectable, we don't want LinkMovementMethod as it conflicts with selection
                if (textView.movementMethod is LinkMovementMethod) {
                    textView.movementMethod = null
                    textView.setTextIsSelectable(true) 
                }
            }

            if (onTextLayout != null) {
                textView.post {
                    onTextLayout(textView.lineCount)
                }
            }
            textView.maxLines = maxLines
            textView.highlightTagsWithClick(onTagClick, isTextSelectable)
        }
    )
}

// TextView 扩展方法，传递回调函数处理点击事件
fun TextView.highlightTagsWithClick(onTagClick: ((String) -> Unit)?, isTextSelectable: Boolean) {
    val originalText = this.text.toString()
    val tagList = TopicUtils.getTopicListByString(originalText)

    // 如果没有找到标签，直接返回
    if (tagList.isEmpty()) return

    // 创建 SpannableString 用于设置文字的不同样式
    val spannableString = SpannableString(this.text)

    val textColor = android.graphics.Color.parseColor("#4D84F7")

    // 遍历匹配到的标签
    for (tag in tagList) {
        val startIndex = originalText.indexOf(tag)
        if (startIndex >= 0) {
            val endIndex = startIndex + tag.length

            // 设置文字颜色为蓝色
            spannableString.setSpan(
                ForegroundColorSpan(textColor),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // 设置点击事件，并重写 updateDrawState 去掉下划线
            spannableString.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // 触发回调，将当前点击的 tag 传递出去
                    onTagClick?.invoke(tag)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    // 设置文字颜色（如果没有 ForegroundColorSpan 也能用这个）
                    ds.color = textColor
                    // 去掉下划线
                    ds.isUnderlineText = false
                }
            }, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    // 将设置好的 SpannableString 应用到 TextView
    this.text = spannableString

    // 使 TextView 支持点击
    if (isTextSelectable) {
        // When selectable, we don't set LinkMovementMethod as it breaks text selection
        // CustomTextView handles clicks manually when selectable
    } else {
        this.movementMethod = LinkMovementMethod.getInstance()
        // 避免点击时背景颜色变化
        this.highlightColor = android.graphics.Color.TRANSPARENT
    }
}

object TopicUtils {

    private val inputReg = "(\\#[\u4e00-\u9fa5a-zA-Z]+\\d{0,100})[\\w\\s]"
    val pattern: Pattern = Pattern.compile(inputReg)
    fun getTopicListByString(text: String): List<String> {
        val tagList: MutableList<String> = mutableListOf()
        val matcher: Matcher = pattern.matcher(text)
        while (matcher.find()) {
            val tag = text.substring(matcher.start(), matcher.end()).trim { it <= ' ' }
            tagList.add(tag)

        }
        return tagList
    }
}
