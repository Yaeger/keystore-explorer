/*
 * Copyright 2004 - 2013 Wayne Grant
 *           2013 - 2014 Kai Kramer
 *
 * This file is part of KeyStore Explorer.
 *
 * KeyStore Explorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * KeyStore Explorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with KeyStore Explorer.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.keystore_explorer.crypto;

/**
 * Enumeration of Security Providers utilized by the crypto utility classes.
 * 
 */
public enum SecurityProvider {
	/** Sun */
	SUN("SUN"),

	/** Bouncy Castle */
	BOUNCY_CASTLE("BC"),

	/** Apple */
	APPLE("Apple"),

	/** Microsoft CAPI */
	MS_CAPI("SunMSCAPI");

	private String jce;

	private SecurityProvider(String jce) {
		this.jce = jce;
	}

	/**
	 * Get SecurityProvider type JCE name.
	 * 
	 * @return JCE name
	 */
	public String jce() {
		return jce;
	}
}