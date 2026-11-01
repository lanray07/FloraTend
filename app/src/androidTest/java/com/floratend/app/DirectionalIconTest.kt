package com.floratend.app

import org.junit.Assert.assertTrue
import org.junit.Test

class DirectionalIconTest {
    @Test fun cardDetailArrowMirrorsWithLayoutDirection() {
        assertTrue("The actual card-detail vector must mirror in RTL", FloraIcons.Detail.autoMirror)
    }
}
