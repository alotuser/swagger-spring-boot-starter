package cn.alotuser.properties;

import java.util.Locale;

import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * SwaggerProperties
 */
@ConfigurationProperties(prefix = "swagger")
public class SwaggerProperties {

	/**
	 * locale fr_FR
	 */
	private Locale locale= Locale.ENGLISH; // 默认使用英语;

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	
}
