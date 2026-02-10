package cn.alotuser.properties;

import java.util.Locale;

import org.springframework.boot.context.properties.ConfigurationProperties;

import cn.alotuser.kit.Constants;
/**
 * SwaggerProperties
 */
@ConfigurationProperties(prefix = Constants.SWAGGER)
public class SwaggerProperties {

	/**
	 * locale fr_FR
	 */
	private Locale locale= Locale.ENGLISH; // 默认使用英语;
	/**
	 * 是否启用 Select a definitions
	 */
	private boolean selectDefinitions = true;
	
	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public boolean isSelectDefinitions() {
		return selectDefinitions;
	}

	public void setSelectDefinitions(boolean selectDefinitions) {
		this.selectDefinitions = selectDefinitions;
	}

	
}
