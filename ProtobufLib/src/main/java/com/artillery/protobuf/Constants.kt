package com.artillery.protobuf

import androidx.annotation.IntRange
import androidx.annotation.Keep
import com.blankj.utilcode.util.LanguageUtils
import java.util.Locale

/**
 * @author : zhiweizhu
 * create on: 2023/7/14 上午11:35
 */

/**
 * 开关
 */
sealed class SwitchType(val value: Int){
    object ON: SwitchType(1)
    object OFF: SwitchType(0)
}


/**
 * 闹钟选择的星期
 */

@Keep
sealed class AlarmChoiceDay(val byte: Int) {
    object Monday : AlarmChoiceDay(0x01)
    object Tuesday : AlarmChoiceDay(0x02)
    object Wednesday : AlarmChoiceDay(0x04)
    object Thursday : AlarmChoiceDay(0x08)
    object Friday : AlarmChoiceDay(0x10)
    object Saturday : AlarmChoiceDay(0x20)
    object Sunday : AlarmChoiceDay(0x40)

    companion object{
        fun of(value: Int): AlarmChoiceDay {
            return when(value){
                0x01 -> Monday
                0x02 -> Tuesday
                0x04 -> Wednesday
                0x08 -> Thursday
                0x10 -> Friday
                0x20 -> Saturday
                0x40 -> Sunday
                else -> Monday
            }
        }
    }
}



sealed class ContactOperate(val value: Int){
    /**
     * 添加
     * serials 无效
     */
    object Add: ContactOperate(1)

    /**
     * 删除
     * name number 无效
     */
    object Del: ContactOperate(2)

    /**
     * 修改
     * serials 无效
     */
    object Edit: ContactOperate(3)

    /**
     * 查看
     * name number 无效
     */
    object Cat: ContactOperate(4)
}

/**
 * 运动类型
 */
sealed class SportMode(val value: Int){
    /**
     * 无运动
     */
    object Nothing: SportMode(0x00)

    /**
     * 户外跑步
     */
    object Run: SportMode(0x01)

    /**
     * 室内跑步
     */
    object RunIndoor: SportMode(0x02)

    /**
     * 户外健走
     */
    object Walk: SportMode(0x03)

    /**
     * 室内健走
     */
    object IndoorWalk: SportMode(0x04)

    /**
     * 越野跑
     */
    object CrossCountryRace: SportMode(0x05)

    /**
     * 户外徒步
     */
    object Hiking: SportMode(0x06)

    /**
     *跑步机
     */
    object Teadmill: SportMode(0x07)
    /**
     *骑行
     */
    object Riding: SportMode(0x08)
    /**
     *室内骑行 (动感单车)
     */
    object Spinning: SportMode(0x09)
    /**
     *小轮车
     */
    object BMX: SportMode(0x0A)
    /**
     *自由训练
     */
    object FreeTraining: SportMode(0x0B)
    /**
     *高强度间歇训练
     */
    object HIIT: SportMode(0x0C)
    /**
     *核心训练
     */
    object CoreTraining: SportMode(0x0D)
    /**
     *有氧训练
     */
    object AerobicTraining: SportMode(0x0E)
    /**
     *无氧训练
     */
    object AnaerobicExercise: SportMode(0x0F)
    /**
     *混合有氧
     */
    object MixedAerobic: SportMode(0x10)
    /**
     *力量训练
     */
    object StrengthTraining: SportMode(0x11)
    /**
     *柔韧训练
     */
    object FlexibilityTraining: SportMode(0x12)
    /**
     *瑜伽
     */
    object Yoga: SportMode(0x13)
    /**
     *拉伸
     */
    object StretchTraining: SportMode(0x14)
    /**
     *普拉提
     */
    object Pilates: SportMode(0x15)
    /**
     *冥想
     */
    object Meditation: SportMode(0x16)
    /**
     *室内健身
     */
    object IndoorFitness: SportMode(0x17)
    /**
     *踏步机
     */
    object Stepper: SportMode(0x18)
    /**
     *椭圆机
     */
    object MachineElliptical: SportMode(0x19)
    /**
     *划船机
     */
    object MachineRowing: SportMode(0x1A)
    /**
     *踏步训练
     */
    object StepTraining: SportMode(0x1B)
    /**
     *体操
     */
    object Gymnastics: SportMode(0x1C)
    /**
     *篮球
     */
    object Basketball: SportMode(0x1D)
    /**
     *足球
     */
    object Football: SportMode(0x1E)
    /**
     *羽毛球
     */
    object Badminton: SportMode(0x1F)
    /**
     *乒乓球
     */
    object PingPong: SportMode(0x20)
    /**
     *网球
     */
    object Tennis: SportMode(0x21)
    /**
     *棒球
     */
    object Baseball: SportMode(0x22)
    /**
     *排球
     */
    object Volleyball: SportMode(0x23)
    /**
     *板球
     */
    object Cricket: SportMode(0x24)
    /**
     *橄榄球
     */
    object Rugby: SportMode(0x25)
    /**
     *曲棍球
     */
    object Hockey: SportMode(0x26)
    /**
     *垒球
     */
    object Softball: SportMode(0x27)
    /**
     *壁球
     */
    object Squash: SportMode(0x28)
    /**
     *手球
     */
    object Handball: SportMode(0x29)
    /**
     *门球
     */
    object Goalball: SportMode(0x2A)
    /**
     *保龄球
     */
    object Bowling: SportMode(0x2B)
    /**
     *沙滩排球
     */
    object BeachVolleyball: SportMode(0x2C)
    /**
     *美式足球
     */
    object AmericanFootball: SportMode(0x2D)
    /**
     *台球
     */
    object Billiards: SportMode(0x2E)
    /**
     *高尔夫
     */
    object Golf: SportMode(0x2F)
    /**
     *登山
     */
    object Climb: SportMode(0x30)
    /**
     *打猎
     */
    object Hunting: SportMode(0x31)
    /**
     *钓鱼
     */
    object Fishing: SportMode(0x32)
    /**
     *帆船运动
     */
    object Sailing: SportMode(0x33)
    /**
     *滑板
     */
    object Skateboard: SportMode(0x34)
    /**
     *滑轮
     */
    object RollerSkating: SportMode(0x35)
    /**
     *划船
     */
    object Boating: SportMode(0x36)
    /**
     *遛狗
     */
    object WalkingDog: SportMode(0x37)
    /**
     *攀岩
     */
    object RockClimbing: SportMode(0x38)
    /**
     *蹦极跳
     */
    object BungeeJump: SportMode(0x39)
    /**
     *跑酷
     */
    object Parkour: SportMode(0x3A)
    /**
     *冲浪
     */
    object Surfing: SportMode(0x3B)
    /**
     *漂流
     */
    object Drift: SportMode(0x3C)
    /**
     *赛艇
     */
    object Rowing: SportMode(0x3D)
    /**
     *摩托艇
     */
    object Motorboat: SportMode(0x3E)
    /**
     *冰壶
     */
    object Curling: SportMode(0x3F)
    /**
     *户外滑冰
     */
    object OutdoorIceSkating: SportMode(0x40)
    /**
     *室内滑冰
     */
    object IndoorIceSkating: SportMode(0x41)
    /**
     *滑雪
     */
    object Skiing: SportMode(0x42)
    /**
     *滑冰
     */
    object Skating: SportMode(0x43)
    /**
     *冬季两项
     */
    object Biathlon: SportMode(0x44)
    /**
     *跳舞
     */
    object Dance: SportMode(0x45)
    /**
     *芭蕾舞
     */
    object Ballet: SportMode(0x46)
    /**
     *肚皮舞
     */
    object BellyDance: SportMode(0x47)
    /**
     *广场舞
     */
    object SquareDancing: SportMode(0x48)
    /**
     *街舞
     */
    object StreetDance: SportMode(0x49)
    /**
     *交谊舞
     */
    object SocialDance: SportMode(0x4A)
    /**
     *尊巴
     */
    object Zumba: SportMode(0x4B)
    /**
     *华尔兹
     */
    object Waltz: SportMode(0x4C)
    /**
     *爵士舞
     */
    object JazzDance: SportMode(0x4D)
    /**
     *拉丁舞
     */
    object LatinDance: SportMode(0x4E)
    /**
     *探戈
     */
    object Tango: SportMode(0x4F)
    /**
     *迪斯科
     */
    object Disco: SportMode(0x50)
    /**
     *踢踏舞
     */
    object TapDance: SportMode(0x51)
    /**
     *武术
     */
    object MartialArts: SportMode(0x52)
    /**
     *自由搏击
     */
    object FreeCombat: SportMode(0x53)
    /**
     *太极
     */
    object TaiChi: SportMode(0x54)
    /**
     *空手道
     */
    object Karate: SportMode(0x55)
    /**
     *柔道
     */
    object Judo: SportMode(0x56)
    /**
     *跆拳道
     */
    object Taekwondo: SportMode(0x57)
    /**
     *拳击
     */
    object Boxing: SportMode(0x58)
    /**
     *摔跤
     */
    object Wrestling: SportMode(0x59)
    /**
     *剑术
     */
    object Swordsmanship: SportMode(0x5A)
    /**
     *泰拳
     */
    object ThaiBoxing: SportMode(0x5B)
    /**
     *游泳
     */
    object Swim: SportMode(0x5C)
    /**
     *跳绳
     */
    object SkippingRope: SportMode(0x5D)
    /**
     *仰卧起坐
     */
    object SitUp: SportMode(0x5E)
    /**
     *开合跳
     */
    object JumpingJack: SportMode(0x5F)
    /**
     *单杠
     */
    object HorizontalBar: SportMode(0x60)
    /**
     *双杠
     */
    object ParallelBars: SportMode(0x61)
    /**
     *骑马
     */
    object RidingHorse: SportMode(0x62)
    /**
     *射箭
     */
    object Archery: SportMode(0x63)
    /**
     *障碍赛
     */
    object ObstacleRace: SportMode(0x64)
    /**
     *飞盘
     */
    object Frisbee: SportMode(0x65)
    /**
     *飞镖
     */
    object Dart: SportMode(0x66)
    /**
     *放风筝
     */
    object FlyingKites: SportMode(0x67)
    /**
     *拔河
     */
    object TugOfWar: SportMode(0x68)
    /**
     *呼啦圈
     */
    object HulaHoop: SportMode(0x69)
    /**
     *爬楼梯
     */
    object ClimbingStairs: SportMode(0x6A)
    /**
     *铁人三项
     */
    object Triathlon: SportMode(0x6B)
    /**
     *躲避球
     */
    object DodgeBall: SportMode(0x6C)

}

/**
 * 运动状态
 */
sealed class SportState(val value: Int){
    /**
     * 准备运动
     */
    object Prepare: SportState(0x00)

    /**
     * 停止运动
     */
    object Stop: SportState(0x01)

    /**
     * 运动开始
     */
    object Start: SportState(0x02)

    /**
     * 运动暂停
     */
    object Pasue: SportState(0x03)

    /**
     * 运动继续
     */
    object Continue: SportState(0x04)

    /**
     * 实时发送数据
     */
    object RealTimeDataTransmission: SportState(0x05)
}


/**
 * 健康数据类型
 */
sealed class HealthDataType(val value: Int){
    /**
     * 步数
     * 每条数据2个字节 每分钟1条
     */
    object Step: HealthDataType(0)

    /**
     * 卡路里
     * 每条数据2个字节 每分钟1条
     */
    object Calorie: HealthDataType(1)
    /**
     * 距离
     * 每条数据2个字节 每分钟1条
     */
    object Distance: HealthDataType(2)

    /**
     * 活动时长
     * 每条数据1个字节 每分钟1条
     */
    object ActivityDuration: HealthDataType(3)

    /**
     * 活动次数
     * 每条数据1个字节 每分钟1条
     */
    object NumberActivities: HealthDataType(4)

    /**
     * 心率
     * 每条数据1个字节 每分钟1条
     */
    object HeartRate: HealthDataType(5)

    /**
     * 血氧
     * 每条数据1个字节 每分钟1条
     */
    object BloodOxygen: HealthDataType(6)

    /**
     * 压力
     * 每条数据1个字节 每分钟1条
     */
    object Pressure: HealthDataType(7)

    /**
     * 睡眠
     * 每条数据1个字节 每分钟1条
     */
    object Sleep: HealthDataType(8)

    /**
     * 心率血氧
     * 每条数据2个字节 每分钟1条
     */
    object HeartRateAndBloodOxygen: HealthDataType(9)

    /**
     * 心率血氧压力
     * 每条数据3个字节 每分钟1条
     */
    object HeartRateBloodOxygenPressure: HealthDataType(10)

    /**
     * 活动时长 活动次数
     * 每条数据2个字节 每分钟1条
     */
    object ActivityNumberDuration: HealthDataType(11)

    /**
     * 步数卡路里距离
     * 每条数据6个字节 每10分钟1条
     */
    object StepsCaloriesDistance: HealthDataType(12)


    /**
     * All
     * 包括 步数 卡路里 距离 活动时长 活动次数 心率 血氧 压力
     *
     * 每条数据11个字节 每10分钟1条
     */
    object All: HealthDataType(13)

    /**
     * 睡眠1
     * 数据返回内容 (类型+长度+类型+长度)
     * 类型 0 清醒 1浅睡 2深睡 类型暂用1个字节长度
     * 长度占用2个字节
     */
    object Sleep1: HealthDataType(14)

    /**
     * 睡眠2
     * 每条数据2个字节 每分钟1条
     * 类型定义 0x31 清醒 0x32 浅睡  0x42 深睡
     */
    object Sleep2: HealthDataType(15)


}


sealed class PhoneCall(val name: String, val number: String, val state: Int){
    /**
     * 来电
     */
    class IncomingCall(name: String, number: String): PhoneCall(name, number, 0)

    /**
     * 挂断
     */
    class HangUp(name: String, number: String): PhoneCall(name, number, 1)

    /**
     * 接听
     */
    class Answer(name: String, number: String): PhoneCall(name, number, 2)

    /**
     * 静音
     */
    class Mute(name: String, number: String): PhoneCall(name, number, 3)
}


/**
 * 设备模式
 */
sealed class DeviceModel(val value: Int){
    /**
     * 恢复出厂设置
     */
    object FactoryReset: DeviceModel(1)

    /**
     * 调试模式
     */
    object DeBug: DeviceModel(2)
}


/**
 * 音乐状态
 */
sealed class MusicState(val value: Int){
    /**
     * 播放
     */
    object Play: MusicState(0)

    /**
     * 暂停
     */
    object Pause: MusicState(1)

    /**
     * 下一首
     */
    object Next: MusicState(3)

    /**
     * 上一首
     */
    object Previous: MusicState(4)
}

/**
 * 气候类型
 */
sealed class Climate(val value: Int){
    /**
     * 晴天
     */
    object Sunny: Climate(0x01)

    /**
     * 多云
     */
    object Clundy: Climate(0x02)

    /**
     * 阴
     */
    object Overcast: Climate(0x03)

    /**
     * 阵雨
     */
    object Shower: Climate(0x04)

    /**
     * 雷阵雨
     */
    object TStorm: Climate(0x05)

    /**
     * 雨夹雪
     */
    object Sleet: Climate(0x06)

    /**
     * 小雨
     */
    object LightRain: Climate(0x07)

    /**
     * 大雨
     */
    object HeavyRain: Climate(0x08)

    /**
     * 雪
     */
    object Snow: Climate(0x09)

    /**
     * 沙尘暴
     */
    object SandStorm: Climate(0x0A)

    /**
     * 雾霾
     */
    object Haze: Climate(0x0B)

    /**
     * 风
     */
    object Windy: Climate(0x0C)
}


/**
 * 如果value 等于 1000说明他文档里面是没有定义的
 */
sealed class MsgType(val value: Int){
    object All: MsgType(0)
    object Instagram: MsgType(11)
    object Linkedin: MsgType(10)
    object Twitter: MsgType(9)
    object FaceBook: MsgType(8)
    object FaceTime: MsgType(1000)
    object Feixin: MsgType(1000)
    object Line: MsgType(14)
    object Sound: MsgType(1000)
    object Gmail: MsgType(21)
    object Webook: MsgType(1000)
    object Wechat: MsgType(3)
    object QQ: MsgType(2)
    object Sms: MsgType(1)
    object Call: MsgType(1000)
    object Skype: MsgType(16)
    object DingTalk: MsgType(5)
    object AliWangWang: MsgType(7)
    object Alipay: MsgType(4)
    object KakaoTalk: MsgType(15)
    object Qianiu: MsgType(6)
    object WhatsApp: MsgType(13)
    object Pinterest: MsgType(12)
    object OtherApp: MsgType(20)
    object Message: MsgType(17)
}

sealed class MessageSwitch(val type: MsgType, @IntRange(from = 0, to = 1) val value: Int){

    class All(enable: Int = 0): MessageSwitch(MsgType.All, enable)
    class Instagram(enable: Int = 0): MessageSwitch(MsgType.Instagram, enable)
    class Linkedin(enable: Int = 0): MessageSwitch(MsgType.Linkedin, enable)
    class Twitter(enable: Int = 0): MessageSwitch(MsgType.Twitter, enable)
    class FaceBook(enable: Int = 0): MessageSwitch(MsgType.FaceBook, enable)
    class FaceTime(enable: Int = 0): MessageSwitch(MsgType.FaceTime, enable)
    class Feixin(enable: Int = 0): MessageSwitch(MsgType.Feixin, enable)
    class Line(enable: Int = 0): MessageSwitch(MsgType.Line, enable)
    class Sound(enable: Int = 0): MessageSwitch(MsgType.Sound, enable)
    class Gmail(enable: Int = 0): MessageSwitch(MsgType.Gmail, enable)
    class Webook(enable: Int = 0): MessageSwitch(MsgType.Webook, enable)
    class Wechat(enable: Int = 0): MessageSwitch(MsgType.Wechat, enable)
    class QQ(enable: Int = 0): MessageSwitch(MsgType.QQ, enable)
    class Sms(enable: Int = 0): MessageSwitch(MsgType.Sms, enable)
    class Call(enable: Int = 0): MessageSwitch(MsgType.Call, enable)
    class Skype(enable: Int = 0): MessageSwitch(MsgType.Skype, enable)
    class DingTalk(enable: Int = 0): MessageSwitch(MsgType.DingTalk, enable)
    class AliWangWang(enable: Int = 0): MessageSwitch(MsgType.AliWangWang, enable)
    class Alipay(enable: Int = 0): MessageSwitch(MsgType.Alipay, enable)
    class KakaoTalk(enable: Int = 0): MessageSwitch(MsgType.KakaoTalk, enable)
    class Qianiu(enable: Int = 0): MessageSwitch(MsgType.Qianiu, enable)
    class WhatsApp(enable: Int = 0): MessageSwitch(MsgType.WhatsApp, enable)
    class Pinterest(enable: Int = 0): MessageSwitch(MsgType.Pinterest, enable)
    class OtherApp(enable: Int = 0): MessageSwitch(MsgType.OtherApp, enable)
    class Message(enable: Int = 0): MessageSwitch(MsgType.Message, enable)

}


/**
 * 语言相关
 */
sealed class JW002Language(val value: Int, val des: String){
    class language_en(value: Int = 1, des: String = "en"): JW002Language(value, des)
    class language_chs(value: Int = 2, des: String = "chs"): JW002Language(value, des)
    class language_cht(value: Int = 3, des: String = "cht"): JW002Language(value, des)
    class language_es(value: Int = 4, des: String = "es"): JW002Language(value, des)
    class language_ru(value: Int = 5, des: String = "ru"): JW002Language(value, des)
    class language_ko(value: Int = 6, des: String = "ko"): JW002Language(value, des)
    class language_fr(value: Int = 7, des: String = "fr"): JW002Language(value, des)
    class language_de(value: Int = 8, des: String = "de"): JW002Language(value, des)
    class language_id(value: Int = 9, des: String = "id"): JW002Language(value, des)
    class language_pl(value: Int = 10, des: String = "pl"): JW002Language(value, des)
    class language_it(value: Int = 11, des: String = "it"): JW002Language(value, des)
    class language_ja(value: Int = 12, des: String = "ja"): JW002Language(value, des)
    class language_th(value: Int = 13, des: String = "th"): JW002Language(value, des)
    class language_ar(value: Int = 14, des: String = "ar"): JW002Language(value, des)
    class language_vi(value: Int = 15, des: String = "vi"): JW002Language(value, des)
    class language_pt(value: Int = 16, des: String = "pt"): JW002Language(value, des)
    class language_nl(value: Int = 17, des: String = "nl"): JW002Language(value, des)
    class language_tr(value: Int = 18, des: String = "tr"): JW002Language(value, des)
    class language_uk(value: Int = 19, des: String = "uk"): JW002Language(value, des)
    class language_he(value: Int = 20, des: String = "he"): JW002Language(value, des)
    class language_pt_br(value: Int = 21, des: String = "pt-br"): JW002Language(value, des)
    class language_ro(value: Int = 22, des: String = "ro"): JW002Language(value, des)
    class language_cs(value: Int = 23, des: String = "cs"): JW002Language(value, des)
    class language_el(value: Int = 24, des: String = "el"): JW002Language(value, des)
    class language_hindi(value: Int = 25, des: String = "hindi"): JW002Language(value, des)
    companion object{
        fun systemLanguage(): JW002Language{
            return when(LanguageUtils.getSystemLanguage().language){
                language_en().des.language() -> language_en()
                language_chs().des.language() -> language_chs()
                language_cht().des.language() -> language_cht()
                language_es().des.language() -> language_es()
                language_ru().des.language() -> language_ru()
                language_ko().des.language() -> language_ko()
                language_fr().des.language() -> language_fr()
                language_de().des.language() -> language_de()
                language_id().des.language() -> language_id()
                language_pl().des.language() -> language_pl()
                language_it().des.language() -> language_it()
                language_ja().des.language() -> language_ja()
                language_th().des.language() -> language_th()
                language_ar().des.language() -> language_ar()
                language_vi().des.language() -> language_vi()
                language_pt().des.language() -> language_pt()
                language_nl().des.language() -> language_nl()
                language_tr().des.language() -> language_tr()
                language_uk().des.language() -> language_uk()
                language_he().des.language() -> language_he()
                language_pt_br().des.language() -> language_pt_br()
                language_ro().des.language() -> language_ro()
                language_cs().des.language() -> language_cs()
                language_el().des.language() -> language_el()
                language_hindi().des.language() -> language_hindi()

                else -> language_chs()
            }
        }
    }
}

fun String.language(): String {
    return Locale(this).language
}