package cn.alotuser.core;

/**
 * Interface for Swagger plugins that resolve i18n text. This interface can be implemented by plugins to provide custom i18n text resolution.
 */
public interface SwaggerMessagePlugin {

	/**
	 * Resolves the i18n text from the given code.
	 *
	 * @param code the value to resolve
	 * @return the resolved i18n text
	 */
	String getMessage(String code);


}
