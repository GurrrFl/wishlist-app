package com.example.wishlistapp.domain.model

/** Accepts either a public URL or the link code entered by the user. */
fun publicLinkCode(input: String): String = input.trim()
    .substringBefore('?')
    .trimEnd('/')
    .substringAfterLast('/')
