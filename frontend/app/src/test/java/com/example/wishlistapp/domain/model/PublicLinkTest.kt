package com.example.wishlistapp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PublicLinkTest {
    @Test fun extractsCodeFromLinkOrCode() {
        assertEquals("53291", publicLinkCode("53291"))
        assertEquals("53291", publicLinkCode(" https://wishlist.app/53291/?source=share "))
        assertEquals("53291", publicLinkCode("wishlist.app/53291"))
    }
}
