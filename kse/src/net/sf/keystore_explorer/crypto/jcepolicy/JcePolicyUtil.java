/*
 * Copyright 2004 - 2013 Wayne Grant
 *           2013 - 2017 Kai Kramer
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
package net.sf.keystore_explorer.crypto.jcepolicy;

import static net.sf.keystore_explorer.crypto.jcepolicy.CryptoStrength.LIMITED;
import static net.sf.keystore_explorer.crypto.jcepolicy.CryptoStrength.UNLIMITED;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.NoSuchAlgorithmException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.crypto.Cipher;

import org.apache.commons.io.IOUtils;

import net.sf.keystore_explorer.crypto.CryptoException;
import net.sf.keystore_explorer.utilities.io.CopyUtil;
import net.sf.keystore_explorer.utilities.net.URLs;
import net.sf.keystore_explorer.version.JavaVersion;

/**
 * Provides utility methods relating to JCE policies.
 *
 */
public class JcePolicyUtil {
	private static ResourceBundle res = ResourceBundle.getBundle("net/sf/keystore_explorer/crypto/jcepolicy/resources");

	private JcePolicyUtil() {
	}

	/**
	 * Is the local JCE policy's crypto strength limited?
	 *
	 * @return True if it is
	 * @throws CryptoException
	 *             If there was a problem getting the policy or crypto strength
	 */
	public static boolean isLocalPolicyCrytoStrengthLimited() throws CryptoException {
		return unlimitedStrengthTest() == CryptoStrength.LIMITED;
	}

	/**
	 * Get a JCE policy's crypto strength.
	 *
	 * @param jcePolicy
	 *            JCE policy
	 * @return Crypto strength
	 * @throws CryptoException
	 *             If there was a problem getting the crypto strength
	 */
	public static CryptoStrength getCryptoStrength(JcePolicy jcePolicy) throws CryptoException {
		try {
			File file = getJarFile(jcePolicy);

			// if there is no policy file at all, we assume that we are running under OpenJDK
			if (!file.exists()) {
				return UNLIMITED;
			}

			JarFile jar = new JarFile(file);

			Manifest jarManifest = jar.getManifest();
			String strength = jarManifest.getMainAttributes().getValue("Crypto-Strength");

			// workaround for IBM JDK: test for maximum key size
			if (strength == null) {
				return unlimitedStrengthTest();
			}

			if (strength.equals(LIMITED.manifestValue())) {
				return LIMITED;
			} else {
				return UNLIMITED;
			}
		} catch (IOException ex) {
			throw new CryptoException(MessageFormat.format(res.getString("NoGetCryptoStrength.exception.message"),
					jcePolicy), ex);
		}
	}

	private static CryptoStrength unlimitedStrengthTest() {
		try {
			if (Cipher.getMaxAllowedKeyLength("AES") >= 256) {
				return UNLIMITED;
			}
		} catch (NoSuchAlgorithmException e) {
			// swallow exception
		}
		return LIMITED;
	}

	/**
	 * Get a JCE policy's details.
	 *
	 * @param jcePolicy
	 *            JCE policy
	 * @return Policy details
	 * @throws CryptoException
	 *             If there was a problem getting the policy details
	 */
	public static String getPolicyDetails(JcePolicy jcePolicy) throws CryptoException {
		try {
			StringWriter sw = new StringWriter();

			File file = getJarFile(jcePolicy);

			// if there is no policy file at all, return empty string
			if (!file.exists()) {
				return "";
			}

			JarFile jar = new JarFile(file);

			Enumeration<JarEntry> jarEntries = jar.entries();

			while (jarEntries.hasMoreElements()) {
				JarEntry jarEntry = jarEntries.nextElement();

				String entryName = jarEntry.getName();

				if (!jarEntry.isDirectory() && entryName.endsWith(".policy")) {
					sw.write(entryName + ":\n\n");

					InputStreamReader isr = null;
					try {
						isr = new InputStreamReader(jar.getInputStream(jarEntry));
						CopyUtil.copy(isr, sw);
					} finally {
						IOUtils.closeQuietly(isr);
					}

					sw.write('\n');
				}
			}

			return sw.toString();
		} catch (IOException ex) {
			throw new CryptoException(MessageFormat.format(res.getString("NoGetPolicyDetails.exception.message"),
					jcePolicy), ex);
		}
	}

	/**
	 * Get a JCE policy's JAR file.
	 *
	 * @param jcePolicy
	 *            JCE policy
	 * @return JAR file
	 */
	public static File getJarFile(JcePolicy jcePolicy) {
		String fileSeperator = System.getProperty("file.separator");
		String javaHome = System.getProperty("java.home");
		File libSecurityFile = new File(javaHome, "lib" + fileSeperator + "security");

		return new File(libSecurityFile, jcePolicy.jar());
	}

	/**
	 * Get JCE Unlimited Strength Jurisdiction Policy download URL for the
	 * current JRE.
	 *
	 * @return Download page URL
	 */
	public static String getJcePolicyDownloadUrl() {
		JavaVersion jreVersion = JavaVersion.getJreVersion();

		int major = jreVersion.getMajor();
		int middle = jreVersion.getMiddle();
		int minor = jreVersion.getMinor();

		String version = MessageFormat.format("{0}.{1}.{2}", major, middle, minor);

		String url = MessageFormat.format(URLs.JCE_POLICY_DOWNLOAD_URL, version);

		return url;
	}


	/**
	 * Hack to disable crypto restrictions until Java 9 is out.
	 *
	 * See http://stackoverflow.com/a/22492582/2672392
	 */
	public static void removeRestrictions() {
		try {
			Class<?> jceSecurityClass = Class.forName("javax.crypto.JceSecurity");
			Class<?> cryptoPermissionsClass = Class.forName("javax.crypto.CryptoPermissions");
			Class<?> cryptoAllPermissionClass = Class.forName("javax.crypto.CryptoAllPermission");

			Field isRestrictedField = jceSecurityClass.getDeclaredField("isRestricted");
			isRestrictedField.setAccessible(true);
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(isRestrictedField, isRestrictedField.getModifiers() & ~Modifier.FINAL);
			isRestrictedField.set(null, false);

			Field defaultPolicyField = jceSecurityClass.getDeclaredField("defaultPolicy");
			defaultPolicyField.setAccessible(true);
			PermissionCollection defaultPolicy = (PermissionCollection) defaultPolicyField.get(null);

			Field permsField = cryptoPermissionsClass.getDeclaredField("perms");
			permsField.setAccessible(true);
			((Map<?, ?>) permsField.get(defaultPolicy)).clear();

			Field cryptoAllPermissionInstanceField = cryptoAllPermissionClass.getDeclaredField("INSTANCE");
			cryptoAllPermissionInstanceField.setAccessible(true);
			defaultPolicy.add((Permission) cryptoAllPermissionInstanceField.get(null));
		} catch (Exception e) {
			// ignore
		}
	}
}
