package com.likeminds.usertagging.util

import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.widget.EditText
import android.widget.TextView

object UserTaggingDecoder {
    private val REGEX_USER_TAGGING = Regex("<<([^<>]+\\|route://\\S+)>>")

    /**
     * Decodes [text] containing the regex, highlights it with [highlightColor] and sets it on [editText] with tagging spans
     */
    @JvmStatic
    fun decode(editText: EditText, text: String?, highlightColor: Int) {
        if (text.isNullOrEmpty()) {
            return
        }
        val matches = REGEX_USER_TAGGING.findAll(text, 0)
        editText.setText(text, TextView.BufferType.EDITABLE)
        matches.toList().reversed().forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last
            val value = matchResult.value
            val tag = value.substring(2, value.length - 2).split("\\|".toRegex())
            val memberName = SpannableString("@${tag[0]}")
            memberName.setSpan(
                MemberTaggingClickableSpan(highlightColor, value),
                0,
                memberName.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            editText.setText(
                editText.editableText.replace(
                    start,
                    end + 1,
                    memberName
                ), TextView.BufferType.EDITABLE
            )
        }
    }

    /**
     * Decodes the given [text], searches for all tagged members and returns a list of Pair
     * containing member id and member name sequentially.
     * @return List<Pair<Member Id, Member Name>>
     */
    @JvmStatic
    fun decodeAndReturnAllTaggedMembers(text: String?): List<Pair<String, String>> {
        if (text.isNullOrEmpty()) {
            return emptyList()
        }
        val matches = REGEX_USER_TAGGING.findAll(text, 0)
        if (matches.count() == 0) {
            return emptyList()
        }
        val result = mutableListOf<Pair<String, String>>()
        matches.forEach { matchResult ->
            val value = matchResult.value
            val tag = value.substring(2, value.length - 2).split("\\|".toRegex())
            val memberName = "@${tag[0]}"
            val memberRoute = tag[1]
            val routeSplits = memberRoute.split("/".toRegex())
            val memberId = routeSplits[routeSplits.size - 1]
            result.add(Pair(memberId, memberName))
        }
        return result
    }

    fun getMemberIdFromRegex(text: String?): String? {
        val uri = getRouteFromRegex(text) ?: return null
        val pathSegments = uri.pathSegments
        if ((uri.host == "member_profile" || uri.host == "member" || uri.host == "user_profile") && pathSegments.size == 1)
            return pathSegments[0]
        return null
    }

    fun getRouteFromRegex(text: String?): Uri? {
        if (text.isNullOrEmpty()) {
            return null
        }
        val match = REGEX_USER_TAGGING.find(text, 0)
        if (match != null) {
            val value = match.value
            val tag = value.substring(2, value.length - 2).split("\\|".toRegex())
            val memberRoute = tag[1]
            return Uri.parse(memberRoute)
        }
        return null
    }
}