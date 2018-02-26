package com.telenor.connect.sms;

import android.support.annotation.NonNull;

import com.telenor.connect.ui.Instruction;

import org.junit.Test;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@Config(sdk = 18)
public class SmsPinParseUtilTest {

    @NonNull
    private Instruction get4DigitPinInstruction() {
        final Instruction instruction = new Instruction();
        instruction.setName(Instruction.PIN_INSTRUCTION_NAME);
        List<Object> list = new ArrayList<>();
        list.add(".* ([0-9]{4}).*");
        list.add(".*([0-9]{4}) .*");
        instruction.setArguments(list);
        return instruction;
    }

    @Test
    public void findsPinInBulgarianSmsWithNoSpaceAfterPeriod() {
        final Instruction instruction = get4DigitPinInstruction();
        String body = "My Contacts:Вашият CONNECT код за потвърждение е 3151.Не го споделяйте";
        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, is("3151"));
    }

    @Test
    public void findsPinInBulgarianSmsWithNoSpaceAfterPeriodAndBalance() {
        final Instruction instruction = get4DigitPinInstruction();
        String body = "BGN2345.56 My Contacts:Вашият CONNECT код за потвърждение е 3151.Не го споделяйте";
        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, is("3151"));
    }

    @Test
    public void findsPinInBulgarianSms() {
        final Instruction instruction = get4DigitPinInstruction();
        String body = "Capture: Вашият CONNECT код за потвърждение е 3151. Не го споделяйте.";
        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, is("3151"));
    }

    @Test
    public void findsPinInEnglishSms() {
        final Instruction instruction = get4DigitPinInstruction();
        String body = "Your verification code for ''HipstaGram'' is 3456 - CONNECT by Telenor Digital.";
        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, is("3456"));
    }

    @Test
    public void findsprefixedPinInEnglishSms() {
        final Instruction instruction = get4DigitPinInstruction();
        String body = "3456 is your verification code for ''HipstaGram''  - CONNECT by Telenor Digital.";
        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, is("3456"));
    }

    @Test
    public void findsPinInJumbledEnglishSms() {
        final Instruction instruction = get4DigitPinInstruction();
        String body = "CONNECT by Telenor Digital: Your verification code for ''HipstaGram'' is 3456";
        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, is("3456"));
    }

    @Test
    public void findsPinInCaptureBrandedSms() {
        final Instruction instruction = get4DigitPinInstruction();
        String body = "Capture: 3456 is your verification code for CONNECT";
        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, is("3456"));
    }

    @Test
    public void findsPinInUnbrandedSms() {
        final Instruction instruction = get4DigitPinInstruction();
        String body = "Your verification code is 0102 - CONNECT by Telenor Digital";
        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, is("0102"));
    }

    @Test
    public void findsPinInPrefixedUnbrandedSms() {
        final Instruction instruction = get4DigitPinInstruction();
        String body = "RM0.00 Your verification code is 0102 - CONNECT by Telenor Digital";
        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, is("0102"));
    }

    @Test
    public void findsPinInPrefixedCaptureBrandedSms() {
        final Instruction instruction = get4DigitPinInstruction();
        String body = "RM0.00 Capture: 0022 is your verification code for CONNECT";
        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, is("0022"));
    }

    @Test
    public void findsPinInPrefixedUnbrandedSmsWithFourDigitBalance() {
        final Instruction instruction = get4DigitPinInstruction();
        String body = "RM1234.56 Your verification code is 0102 - CONNECT by Telenor Digital";
        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, is("0102"));
    }

    @Test
    public void findsPinInPrefixedCaptureBrandedSmsWithFourDigitBalance() {
        final Instruction instruction = get4DigitPinInstruction();
        String body = "RM1234.56 Capture: 0022 is your verification code for CONNECT";
        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, is("0022"));
    }

    @Test
    public void findsPinInPostfixedUnbrandedSmsWithFourDigitBalance() {
        final Instruction instruction = get4DigitPinInstruction();
        String body = "Your verification code is 0102 - CONNECT by Telenor Digital. RM1234.56";
        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, is("0102"));
    }

    @Test
    public void pinWithoutCONNECTInItReturnsNull() {
        final Instruction instruction = get4DigitPinInstruction();
        String body = "Google: 0022 is your verification code";
        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, is(nullValue()));
    }

    @Test
    public void smsWithNonMatchingPatternReturnsNull() {
        final Instruction instruction = get4DigitPinInstruction();
        String body = "RM0.00 Hi. Please click on the link below to change " +
                "your CONNECT password: https://s.telenordigital.com/abcde";
        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, is(nullValue()));
    }

    @Test
    public void findsPinInThaiSms() {
        final Instruction instruction = get4DigitPinInstruction();
        String body = "\u0e23\u0e2b\u0e31\u0e2a\u0e22\u0e37\u0e19\u0e22\u0e31\u0e19\u0e02\u0e2d\u0e07\u0e04\u0e38\u0e13\u0e2a\u0e33\u0e2b\u0e23\u0e31\u0e1a ''HipstaGram'' \u0e04\u0e37\u0e2d 3456 - CONNECT \u0e42\u0e14\u0e22 Telenor Digital";
        // same as: "รหัสยืนยันของคุณสำหรับ ''HipstaGram'' คือ 3456 - CONNECT โดย Telenor Digital";
        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, is("3456"));
    }

    @Test
    public void lookingForMissingPinInEnglishSmsReturnsNull() {
        final Instruction instruction = get4DigitPinInstruction();
        String body = "Telenor are currently having connectivity issues. Expected fix time" +
                " is by 20:00 tonight.";
        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, nullValue());
    }

    @Test
    public void lookingForMissingPinInThaiSmsReturnsNull() {
        final Instruction instruction = get4DigitPinInstruction();
        String body = "\u0e23\u0e2b\u0e31\u0e2a\u0e22\u0e37\u0e19\u0e22\u0e31\u0e19\u0e02\u0e2d\u0e07\u0e04\u0e38\u0e13\u0e2a\u0e33\u0e2b\u0e23\u0e31\u0e1a \u0e04\u0e37\u0e2d  - CONNECT \u0e42\u0e14\u0e22 Telenor Digital";
        // same as: "รหัสยืนยันของคุณสำหรับ คือ  - CONNECT โดย Telenor Digital"
        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, nullValue());
    }

    @Test
    public void findsPinInRightToLeftLanguages() {
        final Instruction instruction = get4DigitPinInstruction();
        String body = "''Hipstagram'' \u06a9\u06d2 \u0644\u06cc\u06d2 \u0627\u0653\u067e \u06a9\u0627 \u062a\u0635\u062f\u06cc\u0642\u06cc \u06a9\u0648\u0688 3456 \u06c1\u06d2 - Telenor Digital \u06a9\u0627 \u067e\u06cc\u0634 \u06a9\u0631\u062f\u06c1 CONNECT\u06d4";
        // ''Hipstagram'' کے لیے آپ کا تصدیقی کوڈ 3456 ہے - Telenor Digital کا پیش کردہ CONNECT۔
        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, is("3456"));
    }

    @Test
    public void lookingForMissingPinInRightToLeftLanguageReturnsNull() {
        final Instruction instruction = get4DigitPinInstruction();
        String body = "\u0627\u0653\u067e \u0646\u06d2 \u063a\u0644\u0637 \u067e\u0627\u0633 \u0648\u0631\u0688 \u062f\u0631\u062c \u06a9\u06cc\u0627 \u06c1\u06d2\u06d4 \u0628\u0631\u0627\u06c1\u0650 \u0645\u06c1\u0631\u0628\u0627\u0646\u06cc \u062f\u0648\u0628\u0627\u0631\u06c1 \u06a9\u0648\u0634\u0634 \u06a9\u0631\u06cc\u06ba \u06cc\u0627 <a href=\"{0}\">\u0627\u067e\u0646\u0627 \u067e\u0627\u0633 \u0648\u0631\u0688 \u062f\u0648\u0628\u0627\u0631\u06c1 \u062a\u0631\u062a\u06cc\u0628 \u062f\u06cc\u06ba</a>\u06d4";
        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, nullValue());
    }

    @Test
    public void findsPinInBurmeseSms() {
        final Instruction instruction = get4DigitPinInstruction();
        String body = "'Hipstagram' ဟာ သင့်ဖုန်းနံပါတ် မှန်ကန်ကြောင်း သက်သေပြမယ့် ကုတ်နံပါတ်က 3456 ဖြစ်ပါတယ်။ Telenor Digital ရဲ့ CONNECT";
        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, is("3456"));
    }

    @Test
    public void lookingForMissingPinInBurmeseReturnsNull() {
        final Instruction instruction = get4DigitPinInstruction();
        String body = "<s>99999999</s> သို့ မှန်ကန်ကြောင်းအတည်ပြုတဲ့ ကုတ်နံပါတ် ပို့ပေးလိုက်ပါပြီ။ ကျေးဇူးပြုပြီး သင့်ဖုန်းနံပါတ်ကို အတည်ပြုဖို့ အဲဒီကုတ်နံပါတ်ကို အောက်ဖက်မှာ ရိုက်ထည့်လိုက်ပါ။";
        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, nullValue());
    }
}
