package com.artillery.rwutils.type

import java.util.Locale

/**
 * @author : zhiweizhu
 * create on: 2023/5/22 下午3:59
 */


/**
 * 时间格式
 */
enum class DayFormatType {
    UNKNOWN,
    HOUR_24,
    HOUR_12
}

/**
 * 语言
 */
enum class LanguageType {
    EN,   //英文
    ZH,   //中文
    ES, //西班牙语
    PT, //葡萄牙语
    FR, //法语
    DE, //德语
    JA, //日语
    RU, //俄语
    PL, //波兰语
    KO, //韩语
    ZH_TW, //繁体中文
    AR, //阿拉伯语
    TH, //泰语
    MS, //马来西亚
    ID, //印度尼西亚
    TR, //土耳其
    IT, //意大利
    EL, //希腊语
    FA, //波斯
    VI, //越南
    NL, //荷兰
    HE //希伯来语
}

fun Locale.getLanguageType(): LanguageType{
    val language = this.language
    return if (language.equals(Locale.ENGLISH.language)){
        LanguageType.EN
    }
    else if(language.equals(Locale.SIMPLIFIED_CHINESE.language)){
        LanguageType.ZH
    }
    else if(language.equals(Locale("es").language)){
        LanguageType.ES
    }
    else if(language.equals(Locale("pt").language)){
        LanguageType.PT
    }
    else if(language.equals(Locale.FRANCE.language)){
        LanguageType.FR
    }
    else if(language.equals(Locale.GERMAN.language)){
        LanguageType.DE
    }
    else if(language.equals(Locale.JAPAN.language)){
        LanguageType.JA
    }
    else if(language.equals(Locale("ru").language)){
        LanguageType.RU
    }
    else if(language.equals(Locale("pl"))){
        LanguageType.PL
    }
    else if(language.equals(Locale.KOREA.language)){
        LanguageType.KO
    }
    else if(language.equals(Locale.TRADITIONAL_CHINESE.language)){
        LanguageType.ZH_TW
    }
    else if(language.equals(Locale("ar").language)){
        LanguageType.AR
    }
    else if(language.equals(Locale("th").language)){
        LanguageType.TH
    }
    else if(language.equals(Locale("ms").language)){
        LanguageType.MS
    }
    else if(language.equals(Locale("id").language)){
        LanguageType.ID
    }
    else if(language.equals(Locale("tr").language)){
        LanguageType.TR
    }
    else if(language.equals(Locale.ITALIAN.language)){
        LanguageType.IT
    }
    else if(language.equals(Locale("el").language)){
        LanguageType.EL
    }
    else if(language.equals(Locale("fa").language)){
        LanguageType.FA
    }
    else if(language.equals(Locale("vi").language)){
        LanguageType.VI
    }
    else if(language.equals(Locale("nl").language)){
        LanguageType.NL
    }
    else if(language.equals(Locale("he").language)){
        LanguageType.HE
    } else {
        LanguageType.ZH
    }
}



/**
 * 是否是中文
 */
enum class ZhType{
    NO_ZH,
    YES_ZH
}

/**
 * 系统类型
 */
enum class SystemType{
    UNKNOWN,
    ANDROID,
    IOS
}

enum class Gender{
    Woman,
    man
}

/**
 * 开关
 */
enum class SwitchType{
    OFF,
    ON
}



