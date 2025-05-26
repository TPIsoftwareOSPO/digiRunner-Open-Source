package tpi.dgrv4.dpaa.component;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.BitSet;

public class UrlCodecHelper {
	
	public static void main(String[] args) throws Exception {
		String urlEncode = "1%206%E4%B8%ADl%E3%80%80l%E6%96%87";
		String plainText = "1 6中l　l文";
		boolean hasEncode = false;
		hasEncode = UrlCodecHelper.hasUrlEncoded(urlEncode);
		System.out.println(hasEncode);
		System.out.println("decode = " + getDecode(urlEncode) + "," + getDecode(urlEncode).equals(plainText));
		hasEncode = UrlCodecHelper.hasUrlEncoded(plainText);
		System.out.println(hasEncode);
		System.out.println("encode = " + getEncode(plainText) + "," + getEncode(plainText).equals(urlEncode));
	}
	
	public static String getEncode(String str) throws Exception {
		String encodedTitle = URLEncoder.encode(str, "UTF-8");
		encodedTitle = encodedTitle.replace("+", "%20");
		return encodedTitle;
	}
	
	public static String getDecode(String str) throws Exception {
		return URLDecoder.decode(str, "UTF-8");
	}
	
	
	private static BitSet dontNeedEncoding;  
	
	static {  
        dontNeedEncoding = new BitSet(256);  
        int i;  
        for (i = 'a'; i <= 'z'; i++) {  
            dontNeedEncoding.set(i);  
        }  
        for (i = 'A'; i <= 'Z'; i++) {  
            dontNeedEncoding.set(i);  
        }  
        for (i = '0'; i <= '9'; i++) {  
            dontNeedEncoding.set(i);  
        }  
        dontNeedEncoding.set('+');  
        /** 
         * 這里會有誤差,比如輸入一個字符串 123+456,它到底是原文就是123+456還是123 456做了urlEncode后的內容呢？<br> 
         * 其實問題是一樣的，比如遇到123%2B456,它到底是原文即使如此，還是123+456 urlEncode后的呢？ <br> 
         * 在這里，我認為只要符合urlEncode規范的，就當作已經urlEncode過了<br> 
         * 畢竟這個方法的初衷就是判斷string是否urlEncode過<br> 
         */  
          
        dontNeedEncoding.set('-');  
        dontNeedEncoding.set('_');  
        dontNeedEncoding.set('.');  
        dontNeedEncoding.set('*');  
    }
	
	/** 
     * 判斷str是否urlEncoder.encode過<br> 
     * 經常遇到這樣的情況，拿到一個URL,但是搞不清楚到底要不要encode.<Br> 
     * 不做encode吧，擔心出錯，做encode吧，又怕重復了<Br> 
     *  
     * @param str 
     * @return 
     */  
    public static boolean hasUrlEncoded(String str) {  
  
        /** 
         * 支持JAVA的URLEncoder.encode出來的string做判斷。 即: 將' '轉成'+' <br> 
         * 0-9a-zA-Z保留 <br> 
         * '-'，'_'，'.'，'*'保留 <br> 
         * 其他字符轉成%XX的格式，X是16進制的大寫字符，范圍是[0-9A-F] 
         */  
        boolean needEncode = false;  
        for (int i = 0; i < str.length(); i++) {  
            char c = str.charAt(i);  
            if (dontNeedEncoding.get((int) c)) {  
                continue;  
            }  
            if (c == '%' && (i + 2) < str.length()) {  
                // 判斷是否符合urlEncode規范  
                char c1 = str.charAt(++i);  
                char c2 = str.charAt(++i);  
                if (isDigit16Char(c1) && isDigit16Char(c2)) {  
                    continue;  
                }  
            }  
            // 其他字符，肯定需要urlEncode  
            needEncode = true;  
            break;  
        }  
  
        return !needEncode;  
    }  
  
    /** 
     * 判斷c是否是16進制的字符 
     *  
     * @param c 
     * @return 
     */  
    private static boolean isDigit16Char(char c) {  
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F');  
    } 
}
