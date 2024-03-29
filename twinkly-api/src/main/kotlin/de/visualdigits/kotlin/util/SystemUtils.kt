package de.visualdigits.kotlin.util

import kotlin.math.min


object SystemUtils {


    // System property constants
    // -----------------------------------------------------------------------
    // These MUST be declared first. Other constants depend on this.
    /**
     * The System property key for the user home directory.
     */
    private const val USER_HOME_KEY = "user.home"

    /**
     * The System property key for the user name.
     */
    private const val USER_NAME_KEY = "user.name"

    /**
     * The System property key for the user directory.
     */
    private const val USER_DIR_KEY = "user.dir"

    /**
     * The System property key for the Java IO temporary directory.
     */
    private const val JAVA_IO_TMPDIR_KEY = "java.io.tmpdir"

    /**
     * The System property key for the Java home directory.
     */
    private const val JAVA_HOME_KEY = "java.home"


    /**
     * The prefix String for all Windows OS.
     */
    private const val OS_NAME_WINDOWS_PREFIX = "Windows"

    /**
     *
     *
     * Is `true` if this is AIX.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 2.0
     */
    val IS_OS_AIX: Boolean = getOsMatchesName("AIX")

    /**
     *
     *
     * Is `true` if this is HP-UX.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 2.0
     */
    val IS_OS_HP_UX: Boolean = getOsMatchesName("HP-UX")

    /**
     *
     *
     * Is `true` if this is IBM OS/400.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.3
     */
    val IS_OS_400: Boolean = getOsMatchesName("OS/400")

    /**
     *
     *
     * Is `true` if this is Irix.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 2.0
     */
    val IS_OS_IRIX: Boolean = getOsMatchesName("Irix")

    /**
     *
     *
     * Is `true` if this is Linux.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 2.0
     */
    val IS_OS_LINUX: Boolean = getOsMatchesName("Linux") || getOsMatchesName("LINUX")

    /**
     *
     *
     * Is `true` if this is Mac.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 2.0
     */
    val IS_OS_MAC: Boolean = getOsMatchesName("Mac")

    /**
     *
     *
     * Is `true` if this is Mac.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 2.0
     */
    val IS_OS_MAC_OSX: Boolean = getOsMatchesName("Mac OS X")

    /**
     *
     *
     * Is `true` if this is Mac OS X Cheetah.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.4
     */
    val IS_OS_MAC_OSX_CHEETAH: Boolean = getOsMatches("Mac OS X", "10.0")

    /**
     *
     *
     * Is `true` if this is Mac OS X Puma.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.4
     */
    val IS_OS_MAC_OSX_PUMA: Boolean = getOsMatches("Mac OS X", "10.1")

    /**
     *
     *
     * Is `true` if this is Mac OS X Jaguar.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.4
     */
    val IS_OS_MAC_OSX_JAGUAR: Boolean = getOsMatches("Mac OS X", "10.2")

    /**
     *
     *
     * Is `true` if this is Mac OS X Panther.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.4
     */
    val IS_OS_MAC_OSX_PANTHER: Boolean = getOsMatches("Mac OS X", "10.3")

    /**
     *
     *
     * Is `true` if this is Mac OS X Tiger.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.4
     */
    val IS_OS_MAC_OSX_TIGER: Boolean = getOsMatches("Mac OS X", "10.4")

    /**
     *
     *
     * Is `true` if this is Mac OS X Leopard.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.4
     */
    val IS_OS_MAC_OSX_LEOPARD: Boolean = getOsMatches("Mac OS X", "10.5")

    /**
     *
     *
     * Is `true` if this is Mac OS X Snow Leopard.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.4
     */
    val IS_OS_MAC_OSX_SNOW_LEOPARD: Boolean = getOsMatches("Mac OS X", "10.6")

    /**
     *
     *
     * Is `true` if this is Mac OS X Lion.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.4
     */
    val IS_OS_MAC_OSX_LION: Boolean = getOsMatches("Mac OS X", "10.7")

    /**
     *
     *
     * Is `true` if this is Mac OS X Mountain Lion.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.4
     */
    val IS_OS_MAC_OSX_MOUNTAIN_LION: Boolean = getOsMatches("Mac OS X", "10.8")

    /**
     *
     *
     * Is `true` if this is Mac OS X Mavericks.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.4
     */
    val IS_OS_MAC_OSX_MAVERICKS: Boolean = getOsMatches("Mac OS X", "10.9")

    /**
     *
     *
     * Is `true` if this is Mac OS X Yosemite.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.4
     */
    val IS_OS_MAC_OSX_YOSEMITE: Boolean = getOsMatches("Mac OS X", "10.10")

    /**
     *
     *
     * Is `true` if this is Mac OS X El Capitan.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.5
     */
    val IS_OS_MAC_OSX_EL_CAPITAN: Boolean = getOsMatches("Mac OS X", "10.11")

    /**
     *
     *
     * Is `true` if this is Mac OS X Sierra.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.12.0
     */
    val IS_OS_MAC_OSX_SIERRA: Boolean = getOsMatches("Mac OS X", "10.12")

    /**
     *
     *
     * Is `true` if this is Mac OS X High Sierra.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.12.0
     */
    val IS_OS_MAC_OSX_HIGH_SIERRA: Boolean = getOsMatches("Mac OS X", "10.13")

    /**
     *
     *
     * Is `true` if this is Mac OS X Mojave.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.12.0
     */
    val IS_OS_MAC_OSX_MOJAVE: Boolean = getOsMatches("Mac OS X", "10.14")

    /**
     *
     *
     * Is `true` if this is Mac OS X Catalina.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.12.0
     */
    val IS_OS_MAC_OSX_CATALINA: Boolean = getOsMatches("Mac OS X", "10.15")

    /**
     *
     *
     * Is `true` if this is Mac OS X Big Sur.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.12.0
     */
    val IS_OS_MAC_OSX_BIG_SUR: Boolean = getOsMatches("Mac OS X", "10.16")

    /**
     *
     *
     * Is `true` if this is FreeBSD.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.1
     */
    val IS_OS_FREE_BSD: Boolean = getOsMatchesName("FreeBSD")

    /**
     *
     *
     * Is `true` if this is OpenBSD.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.1
     */
    val IS_OS_OPEN_BSD: Boolean = getOsMatchesName("OpenBSD")

    /**
     *
     *
     * Is `true` if this is NetBSD.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.1
     */
    val IS_OS_NET_BSD: Boolean = getOsMatchesName("NetBSD")

    /**
     *
     *
     * Is `true` if this is OS/2.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 2.0
     */
    val IS_OS_OS2: Boolean = getOsMatchesName("OS/2")

    /**
     *
     *
     * Is `true` if this is Solaris.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 2.0
     */
    val IS_OS_SOLARIS: Boolean = getOsMatchesName("Solaris")

    /**
     *
     *
     * Is `true` if this is SunOS.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 2.0
     */
    val IS_OS_SUN_OS: Boolean = getOsMatchesName("SunOS")

    /**
     *
     *
     * Is `true` if this is a UNIX like system, as in any of AIX, HP-UX, Irix, Linux, MacOSX, Solaris or SUN OS.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 2.1
     */
    val IS_OS_UNIX: Boolean = (IS_OS_AIX || IS_OS_HP_UX || IS_OS_IRIX || IS_OS_LINUX || IS_OS_MAC_OSX
            || IS_OS_SOLARIS || IS_OS_SUN_OS || IS_OS_FREE_BSD || IS_OS_OPEN_BSD || IS_OS_NET_BSD)

    /**
     *
     *
     * Is `true` if this is Windows.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 2.0
     */
    val IS_OS_WINDOWS: Boolean = getOsMatchesName(OS_NAME_WINDOWS_PREFIX)

    /**
     *
     *
     * Is `true` if this is Windows 2000.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 2.0
     */
    val IS_OS_WINDOWS_2000: Boolean = getOsMatchesName(OS_NAME_WINDOWS_PREFIX + " 2000")

    /**
     *
     *
     * Is `true` if this is Windows 2003.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.1
     */
    val IS_OS_WINDOWS_2003: Boolean = getOsMatchesName(OS_NAME_WINDOWS_PREFIX + " 2003")

    /**
     *
     *
     * Is `true` if this is Windows Server 2008.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.1
     */
    val IS_OS_WINDOWS_2008: Boolean = getOsMatchesName(OS_NAME_WINDOWS_PREFIX + " Server 2008")

    /**
     *
     *
     * Is `true` if this is Windows Server 2012.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.4
     */
    val IS_OS_WINDOWS_2012: Boolean = getOsMatchesName(OS_NAME_WINDOWS_PREFIX + " Server 2012")

    /**
     *
     *
     * Is `true` if this is Windows 95.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 2.0
     */
    val IS_OS_WINDOWS_95: Boolean = getOsMatchesName(OS_NAME_WINDOWS_PREFIX + " 95")

    /**
     *
     *
     * Is `true` if this is Windows 98.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 2.0
     */
    val IS_OS_WINDOWS_98: Boolean = getOsMatchesName(OS_NAME_WINDOWS_PREFIX + " 98")

    /**
     *
     *
     * Is `true` if this is Windows ME.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 2.0
     */
    val IS_OS_WINDOWS_ME: Boolean = getOsMatchesName(OS_NAME_WINDOWS_PREFIX + " Me")

    /**
     *
     *
     * Is `true` if this is Windows NT.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 2.0
     */
    val IS_OS_WINDOWS_NT: Boolean = getOsMatchesName(OS_NAME_WINDOWS_PREFIX + " NT")

    /**
     *
     *
     * Is `true` if this is Windows XP.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 2.0
     */
    val IS_OS_WINDOWS_XP: Boolean = getOsMatchesName(OS_NAME_WINDOWS_PREFIX + " XP")


    // -----------------------------------------------------------------------
    /**
     *
     *
     * Is `true` if this is Windows Vista.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 2.4
     */
    val IS_OS_WINDOWS_VISTA: Boolean = getOsMatchesName(OS_NAME_WINDOWS_PREFIX + " Vista")

    /**
     *
     *
     * Is `true` if this is Windows 7.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.0
     */
    val IS_OS_WINDOWS_7: Boolean = getOsMatchesName(OS_NAME_WINDOWS_PREFIX + " 7")

    /**
     *
     *
     * Is `true` if this is Windows 8.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.2
     */
    val IS_OS_WINDOWS_8: Boolean = getOsMatchesName(OS_NAME_WINDOWS_PREFIX + " 8")

    /**
     *
     *
     * Is `true` if this is Windows 10.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.5
     */
    val IS_OS_WINDOWS_10: Boolean = getOsMatchesName(OS_NAME_WINDOWS_PREFIX + " 10")

    /**
     *
     *
     * Is `true` if this is z/OS.
     *
     *
     *
     * The field will return `false` if `OS_NAME` is `null`.
     *
     *
     * @since 3.5
     */
    // Values on a z/OS system I tested (Gary Gregory - 2016-03-12)
    // os.arch = s390x
    // os.encoding = ISO8859_1
    // os.name = z/OS
    // os.version = 02.02.00
    val IS_OS_ZOS: Boolean = getOsMatchesName("z/OS")


    /**
     *
     *
     * The `os.name` System Property. Operating system name.
     *
     *
     *
     * Defaults to `null` if the runtime does not have security access to read this property or the property does
     * not exist.
     *
     *
     *
     * This value is initialized when the class is loaded. If [System.setProperty] or
     * [System.setProperties] is called after this class is loaded, the value will be out of
     * sync with that System property.
     *
     *
     * @since Java 1.1
     */
    val OS_NAME: String = getSystemProperty("os.name")!!

    /**
     *
     *
     * The `os.version` System Property. Operating system version.
     *
     *
     *
     * Defaults to `null` if the runtime does not have security access to read this property or the property does
     * not exist.
     *
     *
     *
     * This value is initialized when the class is loaded. If [System.setProperty] or
     * [System.setProperties] is called after this class is loaded, the value will be out of
     * sync with that System property.
     *
     *
     * @since Java 1.1
     */
    val OS_VERSION: String = getSystemProperty("os.version")!!

    /**
     *
     *
     * The `user.dir` System Property. User's current working directory.
     *
     *
     *
     * Defaults to `null` if the runtime does not have security access to read this property or the property does
     * not exist.
     *
     *
     *
     * This value is initialized when the class is loaded. If [System.setProperty] or
     * [System.setProperties] is called after this class is loaded, the value will be out of
     * sync with that System property.
     *
     *
     * @since Java 1.1
     */
    val USER_DIR: String? = getSystemProperty(USER_DIR_KEY)

    /**
     *
     *
     * The `user.home` System Property. User's home directory.
     *
     *
     *
     * Defaults to `null` if the runtime does not have security access to read this property or the property does
     * not exist.
     *
     *
     *
     * This value is initialized when the class is loaded. If [System.setProperty] or
     * [System.setProperties] is called after this class is loaded, the value will be out of
     * sync with that System property.
     *
     *
     * @since Java 1.1
     */
    val USER_HOME: String? = getSystemProperty(USER_HOME_KEY)

    /**
     * Decides if the operating system matches.
     *
     * @param osNamePrefix the prefix for the OS name
     * @param osVersionPrefix the prefix for the version
     * @return true if matches, or false if not or can't determine
     */
    private fun getOsMatches(osNamePrefix: String, osVersionPrefix: String): Boolean {
        return isOSMatch(OS_NAME, OS_VERSION, osNamePrefix, osVersionPrefix)
    }

    /**
     * Decides if the operating system matches.
     *
     *
     * This method is package private instead of private to support unit test invocation.
     *
     *
     * @param osName the actual OS name
     * @param osVersion the actual OS version
     * @param osNamePrefix the prefix for the expected OS name
     * @param osVersionPrefix the prefix for the expected OS version
     * @return true if matches, or false if not or can't determine
     */
    fun isOSMatch(osName: String, osVersion: String, osNamePrefix: String, osVersionPrefix: String): Boolean {
        if (osName == null || osVersion == null) {
            return false
        }
        return isOSNameMatch(osName, osNamePrefix) && isOSVersionMatch(osVersion, osVersionPrefix)
    }

    /**
     * Decides if the operating system version matches.
     *
     *
     * This method is package private instead of private to support unit test invocation.
     *
     *
     * @param osVersion the actual OS version
     * @param osVersionPrefix the prefix for the expected OS version
     * @return true if matches, or false if not or can't determine
     */
    fun isOSVersionMatch(osVersion: String, osVersionPrefix: String): Boolean {
        if (osVersion.isEmpty()) {
            return false
        }
        // Compare parts of the version string instead of using String.startsWith(String) because otherwise
        // osVersionPrefix 10.1 would also match osVersion 10.10
        val versionPrefixParts = osVersionPrefix.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val versionParts = osVersion.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (i in 0 until min(versionPrefixParts.size.toDouble(), versionParts.size.toDouble()).toInt()) {
            if (versionPrefixParts[i] != versionParts[i]) {
                return false
            }
        }
        return true
    }

    /**
     * Decides if the operating system matches.
     *
     * @param osNamePrefix the prefix for the OS name
     * @return true if matches, or false if not or can't determine
     */
    private fun getOsMatchesName(osNamePrefix: String): Boolean {
        return isOSNameMatch(OS_NAME, osNamePrefix)
    }

    /**
     * Decides if the operating system matches.
     *
     *
     * This method is package private instead of private to support unit test invocation.
     *
     *
     * @param osName the actual OS name
     * @param osNamePrefix the prefix for the expected OS name
     * @return true if matches, or false if not or can't determine
     */
    fun isOSNameMatch(osName: String, osNamePrefix: String): Boolean {
        if (osName == null) {
            return false
        }
        return osName.startsWith(osNamePrefix!!)
    }
    // -----------------------------------------------------------------------
    /**
     *
     *
     * Gets a System property, defaulting to `null` if the property cannot be read.
     *
     *
     *
     * If a `SecurityException` is caught, the return value is `null` and a message is written to
     * `System.err`.
     *
     *
     * @param property the system property name
     * @return the system property value or `null` if a security problem occurs
     */
    private fun getSystemProperty(property: String): String? {
        return try {
            System.getProperty(property)
        } catch (ex: SecurityException) {
            // we are not allowed to look at this property
            // System.err.println("Caught a SecurityException reading the system property '" + property
            // + "'; the SystemUtils property value will default to null.");
            null
        }
    }
}
