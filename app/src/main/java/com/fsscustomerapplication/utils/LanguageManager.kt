package com.fsscustomerapplication.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.fsscustomerapplication.data.local.SessionManager
import java.util.Locale

data class AppLanguage(
    val code: String,
    val name: String,
    val nativeName: String,
    val flagEmoji: String = "🇮🇳"
)

object LanguageManager {

    val indianLanguages = listOf(
        AppLanguage("en", "English", "English", "🇬🇧"),
        AppLanguage("hi", "Hindi", "हिन्दी", "🇮🇳"),
        AppLanguage("mr", "Marathi", "मराठी", "🇮🇳"),
        AppLanguage("gu", "Gujarati", "ગુજરાતી", "🇮🇳"),
        AppLanguage("ta", "Tamil", "தமிழ்", "🇮🇳"),
        AppLanguage("te", "Telugu", "తెలుగు", "🇮🇳"),
        AppLanguage("kn", "Kannada", "ಕನ್ನಡ", "🇮🇳"),
        AppLanguage("ml", "Malayalam", "മലയാളം", "🇮🇳"),
        AppLanguage("bn", "Bengali", "বাংলা", "🇮🇳"),
        AppLanguage("pa", "Punjabi", "ਪੰਜਾਬੀ", "🇮🇳")
    )

    fun getLanguageByCode(code: String): AppLanguage {
        return indianLanguages.find { it.code.equals(code, ignoreCase = true) } ?: indianLanguages.first()
    }

    fun applyLanguage(context: Context, languageCode: String): Context {
        val sessionManager = SessionManager(context)
        sessionManager.saveLanguage(languageCode)

        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        return context.createConfigurationContext(config)
    }

    fun findActivity(context: Context): Activity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    // Common Translations Map supporting both keys (e.g. "our_products") & phrase strings ("Our Products")
    private val rawTranslations = mapOf(
        "welcome_dashboard" to mapOf(
            "en" to "Welcome to your dashboard", "hi" to "आपके डैशबोर्ड में स्वागत है", "mr" to "तुमच्या डॅशबोर्डवर स्वागत आहे",
            "gu" to "તમારા ડૅશબોર્ડમાં સ્વાગત છે", "ta" to "உங்கள் டாஷ்போர்டிற்கு வரவேற்கிறோம்", "te" to "మీ డాష్‌బోర్డ్‌కు స్వాగతం",
            "kn" to "ನಿಮ್ಮ ಡ್ಯಾಶ್‌ಬೋರ್ಡ್‌ಗೆ ಸ್ವಾಗತ", "ml" to "നിങ്ങളുടെ ഡാഷ്‌ബോർഡിലേക്ക് സ്വാഗതം", "bn" to "আপনার ড্যাশবোর্ডে স্বাগতম", "pa" to "ਤੁਹਾਡੇ ਡੈਸ਼ਬੋਰਡ ਵਿੱਚ ਜੀ ਆਇਆਂ ਨੂੰ"
        ),
        "our_products" to mapOf(
            "en" to "Our Products", "hi" to "हमारे उत्पाद", "mr" to "आमची उत्पादने",
            "gu" to "અમારા ઉત્પાદનો", "ta" to "எங்கள் தயாரிப்புகள்", "te" to "మా ఉత్పత్తులు",
            "kn" to "ನಮ್ಮ ಉತ್ಪನ್ನಗಳು", "ml" to "ഞങ്ങളുടെ ഉൽപ്പന്നങ്ങൾ", "bn" to "আমাদের পণ্য", "pa" to "ਸਾਡੇ ਉਤਪਾਦ"
        ),
        "our_services" to mapOf(
            "en" to "Our Services", "hi" to "हमारी सेवाएं", "mr" to "आमच्या सेवा",
            "gu" to "અમારી સેવાઓ", "ta" to "எங்கள் சேவைகள்", "te" to "మా సేవలు",
            "kn" to "ನಮ್ಮ ಸೇವೆಗಳು", "ml" to "ഞങ്ങളുടെ സേവനങ്ങൾ", "bn" to "আমাদের পরিষেবা", "pa" to "ਸਾਡੀਆਂ ਸੇਵਾਵਾਂ"
        ),
        "your_licences" to mapOf(
            "en" to "Your Licences", "hi" to "आपके लाइसेंस", "mr" to "तुमचे परवाने",
            "gu" to "તમારા લાયસન્સ", "ta" to "உங்கள் உரிமங்கள்", "te" to "మీ లైసెన్సులు",
            "kn" to "ನಿಮ್ಮ ಪರವಾನಗಿಗಳು", "ml" to "നിങ്ങളുടെ ലൈസൻസുകൾ", "bn" to "আপনার লাইসেন্স", "pa" to "ਤੁਹਾਡੇ ਲਾਇਸੈਂਸ"
        ),
        "my_licences" to mapOf(
            "en" to "My Licences", "hi" to "मेरे लाइसेंस", "mr" to "माझे परवाने",
            "gu" to "મારા લાયસન્સ", "ta" to "என் உரிமங்கள்", "te" to "నా లైసెన్సులు",
            "kn" to "ನನ್ನ ಪರವಾನಗಿಗಳು", "ml" to "എന്റെ ലൈസൻസുകൾ", "bn" to "আমার লাইসেন্স", "pa" to "ਮੇਰੇ ਲਾਇਸੈਂਸ"
        ),
        "dashboard" to mapOf(
            "en" to "Dashboard", "hi" to "डैशबोर्ड", "mr" to "डॅशबोर्ड",
            "gu" to "ડૅશબોર્ડ", "ta" to "டாஷ்போர்டு", "te" to "డాష్‌బోర్డ్",
            "kn" to "ಡ್ಯಾಶ್‌ಬೋರ್ಡ್", "ml" to "ഡാഷ്‌ബോർഡ്", "bn" to "ড্যাশবোর্ড", "pa" to "ਡੈਸ਼ਬੋਰਡ"
        ),
        "home" to mapOf(
            "en" to "Home", "hi" to "होम", "mr" to "होम",
            "gu" to "હોમ", "ta" to "முகப்பு", "te" to "హోమ్",
            "kn" to "ಮುಖಪುಟ", "ml" to "ഹോം", "bn" to "হোম", "pa" to "ਹੋਮ"
        ),
        "profile" to mapOf(
            "en" to "Profile", "hi" to "प्रोफाइल", "mr" to "प्रोफाइल",
            "gu" to "પ્રોફાઇલ", "ta" to "சுயவிவரம்", "te" to "પ્રொఫైల్",
            "kn" to "ಪ್ರೊಫೈಲ್", "ml" to "പ്രൊഫൈൽ", "bn" to "প্রোফাইল", "pa" to "ਪ੍ਰੋਫਾਈਲ"
        ),
        "my_tickets" to mapOf(
            "en" to "My Tickets", "hi" to "मेरे टिकट्स", "mr" to "माझे तिकीट",
            "gu" to "મારી ટિકિટો", "ta" to "என் டிக்கெட்டுகள்", "te" to "నా టిక్కెట్లు",
            "kn" to "ನನ್ನ ಟಿಕೆಟ್‌ಗಳು", "ml" to "എന്റെ ടിക്കറ്റുകൾ", "bn" to "আমার টিকিট", "pa" to "ਮੇਰੀਆਂ ਟਿਕਟਾਂ"
        ),
        "personal_info" to mapOf(
            "en" to "Personal Information", "hi" to "व्यक्तिगत जानकारी", "mr" to "वैयक्तिक माहिती",
            "gu" to "વ્યક્તિગત માહિતી", "ta" to "தனிப்பட்ட தகவல்", "te" to "వ్యక్తిగత సమాచారం",
            "kn" to "ವೈಯಕ್ತಿಕ ಮಾಹಿತಿ", "ml" to "വ്യക്തിഗത വിവരങ്ങൾ", "bn" to "ব্যক্তিগত তথ্য", "pa" to "ਨਿੱਜੀ ਜਾਣਕਾਰੀ"
        ),
        "company_info" to mapOf(
            "en" to "Company Information", "hi" to "कंपनी की जानकारी", "mr" to "कंपनीची माहिती",
            "gu" to "કંપનીની માહિતી", "ta" to "நிறுவனத்தின் தகவல்", "te" to "కంపెనీ సమాచారం",
            "kn" to "ಕಂಪನಿ ಮಾಹಿತಿ", "ml" to "കമ്പനി വിവരങ്ങൾ", "bn" to "কোম্পানির তথ্য", "pa" to "ਕੰਪਨੀ ਦੀ ਜਾਣਕਾਰੀ"
        ),
        "manage_admins" to mapOf(
            "en" to "Manage Admins", "hi" to "एडमिन प्रबंधित करें", "mr" to "ऍडमिन व्यवस्थापित करा",
            "gu" to "એડમિન મેનેજ કરો", "ta" to "நிர்வாகிகளை நிர்வகிக்கவும்", "te" to "అడ్మిన్‌లను నిర్వాహించండి",
            "kn" to "ಅಡ್ಮಿನ್‌ಗಳನ್ನು ನಿರ್ವಹಿಸಿ", "ml" to "അഡ്മിൻമാരെ മാനേജ് ചെയ്യുക", "bn" to "অ্যাডমিন পরিচালনা করুন", "pa" to "ਐਡਮਿਨ ਮੈਨੇਜ ਕਰੋ"
        ),
        "change_password" to mapOf(
            "en" to "Change Password", "hi" to "पासवर्ड बदलें", "mr" to "पासवर्ड बदला",
            "gu" to "પાસવર્ડ બદલો", "ta" to "கடவுச்சொல்லை மாற்றவும்", "te" to "పాస్‌వర్డ్ మార్చండి",
            "kn" to "ಪಾಸ್‌ವರ್ಡ್ ಬದಲಾಯಿಸಿ", "ml" to "പാസ്‌വേഡ് മാറ്റുക", "bn" to "পাসওয়ার্ড পরিবর্তন করুন", "pa" to "ਪਾਸਵਰਡ ਬਦਲੋ"
        ),
        "about_us" to mapOf(
            "en" to "About Us", "hi" to "हमारे बारे में", "mr" to "आमच्याबद्दल",
            "gu" to "અમારા વિશે", "ta" to "எங்களைப் பற்றி", "te" to "మా గురించి",
            "kn" to "ನಮ್ಮ ಬಗ್ಗೆ", "ml" to "ഞങ്ങളെക്കുറിച്ച്", "bn" to "আমাদের সম্পর্কে", "pa" to "ਸਾਡੇ ਬਾਰੇ"
        ),
        "change_language" to mapOf(
            "en" to "App Language", "hi" to "भाषा (Language)", "mr" to "भाषा (Language)",
            "gu" to "ભાષા (Language)", "ta" to "மொழி (Language)", "te" to "భాష (Language)",
            "kn" to "ಭಾಷೆ (Language)", "ml" to "ഭാഷ (Language)", "bn" to "भाषा (Language)", "pa" to "ਭਾਸ਼ਾ (Language)"
        ),
        "logout" to mapOf(
            "en" to "Logout", "hi" to "लॉग आउट", "mr" to "लॉग आउट",
            "gu" to "લૉગ આઉટ", "ta" to "வெளியேறு", "te" to "లాగ్ అవుట్",
            "kn" to "ಲಾಗ್ ಔಟ್", "ml" to "ലോഗ് ഔട്ട്", "bn" to "লগ আউট", "pa" to "ਲੌਗ ਆਉਟ"
        ),
        "active_services" to mapOf(
            "en" to "Active Services", "hi" to "सक्रिय सेवाएं", "mr" to "सक्रिय सेवा",
            "gu" to "સક્રિય સેવાઓ", "ta" to "செயலில் உள்ள சேவைகள்", "te" to "యాక్టివ్ సేవలు",
            "kn" to "ಸಕ್ರಿಯ ಸೇವೆಗಳು", "ml" to "സജീവ സേവനങ്ങൾ", "bn" to "সক্রিয় পরিষেবা", "pa" to "ਐਕਟਿਵ ਸੇਵਾਵਾਂ"
        ),
        "pending_requests" to mapOf(
            "en" to "Pending Requests", "hi" to "लंबित अनुरोध", "mr" to "प्रलंबित विनंत्या",
            "gu" to "બાકી રહેલી વિનંતીઓ", "ta" to "நிலુவையில் உள்ள கோரிக்கைகள்", "te" to "పెండింగ్ అభ్యర్థనలు",
            "kn" to "ಬಾಕಿ ವಿನಂತಿಗಳು", "ml" to "തീർപ്പാക്കാത്ത അഭ്യർത്ഥനകൾ", "bn" to "বকেয়া অনুরোধ", "pa" to "ਬਕਾਇਆ ਬੇਨਤੀਆਂ"
        ),
        "support_tickets" to mapOf(
            "en" to "Support Tickets", "hi" to "सपोर्ट टिकट", "mr" to "सपोर्ट तिकीट",
            "gu" to "સપોર્ટ ટિકિટ", "ta" to "ஆதரவு டிக்கெட்டுகள்", "te" to "సపోర్ట్ టిక్కెట్లు",
            "kn" to "ಬೆಂಬಲ ಟಿಕೆಟ್‌ಗಳು", "ml" to "സപ്പോർട്ട് Ticket", "bn" to "সহায়তা টিকিট", "pa" to "ਸਪੋਰਟ ਟਿਕਟਾਂ"
        ),
        "invoices" to mapOf(
            "en" to "Invoices", "hi" to "चालान", "mr" to "पावत्या",
            "gu" to "ઇન્વૉઇસેસ", "ta" to "ரசீதுகள்", "te" to "ఇన్వాయిస్లు",
            "kn" to "ಇನ್‌ವಾಯ್ಸ್‌ಗಳು", "ml" to "ഇൻവോയ്സുകൾ", "bn" to "ইনভয়েস", "pa" to "ਇਨਵੌਇਸ"
        ),
        "renew_now" to mapOf(
            "en" to "Renew Now", "hi" to "अभी नवीनीकृत करें", "mr" to "आता नूतनीकरण करा",
            "gu" to "હવે રિન્યૂ કરો", "ta" to "இப்போது புதுப்பிக்கவும்", "te" to "ఇప్పుడే పునరుద్ధరించండి",
            "kn" to "ಈಗ ನವೀಕರಿಸಿ", "ml" to "ഇപ്പോൾ പുതുക്കുക", "bn" to "এখনই পুনর্নবীকরণ করুন", "pa" to "ਹੁਣੇ ਨਵੀਨੀਕਰਨ ਕਰੋ"
        ),
        "support" to mapOf(
            "en" to "Support", "hi" to "सहायता", "mr" to "सपोर्ट",
            "gu" to "સપોર્ટ", "ta" to "ஆதரவு", "te" to "సపోర్ట్",
            "kn" to "ಬೆಂಬಲ", "ml" to "പിന്തുണ", "bn" to "সহায়তা", "pa" to "ਸਪੋਰਟ"
        ),
        "all_set" to mapOf(
            "en" to "All Set! ✅", "hi" to "सब तैयार है! ✅", "mr" to "सर्व तयार आहे! ✅",
            "gu" to "બધું તૈયાર છે! ✅", "ta" to "எல்லாம் தயார்! ✅", "te" to "అంతా సిద్ధం! ✅",
            "kn" to "ಎಲ್ಲವೂ ಸಿದ್ಧವಾಗಿದೆ! ✅", "ml" to "എല്ലാം സജ്ജമാണ്! ✅", "bn" to "সব তৈরি! ✅", "pa" to "ਸਭ ਤਿਆਰ ਹੈ! ✅"
        ),
        "all_set_sub" to mapOf(
            "en" to "Your subscriptions are active and secure", "hi" to "आपकी सदस्यताएँ सक्रिय और सुरक्षित हैं", "mr" to "तुमची वर्गणी सक्रिय आणि सुरक्षित आहे",
            "gu" to "તમારા સબ્સ્ક્રિપ્શન્સ સક્રિય અને સુરક્ષિત છે", "ta" to "உங்கள் சந்தாக்கள் செயலில் மற்றும் பாதுகாப்பாக உள்ளன", "te" to "మీ సభ్యత్వాలు సక్రియంగా మరియు సురક્ષితంగా ఉన్నాయి",
            "kn" to "ನಿಮ್ಮ ಚಂದಾದಾರಿಕೆಗಳು ಸಕ್ರಿಯವಾಗಿವೆ ಮತ್ತು ಸುರಕ್ಷಿತವಾಗಿವೆ", "ml" to "നിങ്ങളുടെ സബ്‌സ്‌ക്രിപ്‌ഷനുകൾ സജീവവും സുരക്ഷിതവുമാണ്", "bn" to "আপনার সদস্যতা সক্রিয় এবং নিরাপদ", "pa" to "ਤੁਹਾਡੀਆਂ ਸਬਸਕ੍ਰਿਪਸ਼ਨਾਂ ਐਕਟਿਵ ਅਤੇ ਸੁਰੱਖਿਅਤ ਹਨ"
        )
    )

    // Build unified map indexing both "our_products" and "our products"
    private val phraseMap: Map<String, Map<String, String>> by lazy {
        val map = mutableMapOf<String, Map<String, String>>()
        rawTranslations.forEach { (key, langMap) ->
            map[key.lowercase()] = langMap
            // Also map key with spaces (e.g., "welcome_dashboard" -> "welcome dashboard")
            val spaceKey = key.replace("_", " ").lowercase()
            if (!map.containsKey(spaceKey)) {
                map[spaceKey] = langMap
            }
            // Also map English value if available (e.g. "welcome to your dashboard" -> langMap)
            langMap["en"]?.let { enVal ->
                val enKey = enVal.lowercase()
                if (!map.containsKey(enKey)) {
                    map[enKey] = langMap
                }
            }
        }
        map
    }

    fun tr(key: String, langCode: String): String {
        val normKey = key.trim().lowercase()
        val matchMap = phraseMap[normKey]

        if (matchMap != null) {
            val translated = matchMap[langCode] ?: matchMap["en"]
            if (!translated.isNullOrBlank()) return translated
        }

        // Fallback: if key is snake_case like "welcome_dashboard", turn it into "Welcome Dashboard"
        if (key.contains("_")) {
            return key.split("_").joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            }
        }

        return key
    }
}

/**
 * Compose helper to automatically translate any key/text to the current customer language
 */
@Composable
fun tr(text: String): String {
    val context = LocalContext.current
    val langCode = remember(context) { SessionManager(context).getLanguage() }
    return LanguageManager.tr(text, langCode)
}
