package com.example.wishlistapp.data

import com.example.wishlistapp.data.demo.DemoAccount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionManagerDemoTest {
    @Test fun demoCanSwitchPersonasWithoutCreatingServerToken() {
        val prefs = inMemoryPreferences()
        val session = SessionManager(prefs, allowDemo = true)

        session.enterDemo(DemoAccount.ALEKSANDRA)
        assertTrue(session.isLoggedIn())
        assertTrue(session.isDemoSession())
        assertNull(session.getToken())
        assertEquals(1, session.getUserId())

        session.enterDemo(DemoAccount.KATE)
        assertEquals(2, session.userId.value)
        session.clear()
        assertFalse(session.isLoggedIn())
        assertNull(session.getUserId())
    }

    @Test fun networkConfigurationCannotUsePersistedDemoSession() {
        val prefs = inMemoryPreferences()
        SessionManager(prefs, allowDemo = true).enterDemo(DemoAccount.ALEKSANDRA)
        val networkSession = SessionManager(prefs, allowDemo = false)

        assertFalse(networkSession.isLoggedIn())
        assertNull(networkSession.getUserId())
        assertTrue(runCatching { networkSession.enterDemo(DemoAccount.KATE) }.isFailure)
    }
}
