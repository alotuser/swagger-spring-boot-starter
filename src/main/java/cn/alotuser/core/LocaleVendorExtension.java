package cn.alotuser.core;

import java.util.Locale;
import java.util.Objects;

import springfox.documentation.service.VendorExtension;

public class LocaleVendorExtension implements VendorExtension<Locale> {
	private String name;
	private Locale value;

	public LocaleVendorExtension(String name, Locale value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Locale getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		LocaleVendorExtension that = (LocaleVendorExtension) o;
		return Objects.equals(name, that.name) && Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, value);
	}

	@Override
	public String toString() {
		return new StringBuffer(this.getClass().getSimpleName()).append("{").append("name").append(name).append(", ").append("value").append(value).append(", ").append("}").toString();
	}
}
