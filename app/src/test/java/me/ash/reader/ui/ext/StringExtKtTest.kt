package me.ash.reader.ui.ext

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class StringExtTest {

    @Test
    fun testExtractDomain() {
        Assert.assertEquals(null, "".extractDomain())
        Assert.assertEquals(null, null.extractDomain())
        var case = "https://ash7.io"
        Assert.assertEquals("ash7.io", case.extractDomain())
        case = "ash7.io"
        Assert.assertEquals("ash7.io", case.extractDomain())
        case = "https://ash7.io/blog/hello/"
        Assert.assertEquals("ash7.io", case.extractDomain())
        case = "http://ash7.io/blog/hello/"
        Assert.assertEquals("ash7.io", case.extractDomain())
        case = "file://ash7.io/blog"
        Assert.assertEquals("ash7.io", case.extractDomain())
        case = "file://127.0.0.1/blog"
        Assert.assertEquals("127.0.0.1", case.extractDomain())
        case = "ftp://127.0.0.1"
        Assert.assertEquals("127.0.0.1", case.extractDomain())
    }
}
