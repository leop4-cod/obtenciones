/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package senadi.gob.ec.adminob.util;

/**
 *
 * @author Michael Yanangómez
 */
/**
 * 
 */

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;


public class BaseController implements Serializable {
	private static final long serialVersionUID = -2713508037042573416L;

	public static final Locale DEFAULT_LOCALE = new Locale("es", "EC");// US
	// separador
	// decimal
	// punto
	// (.) y
	// EC
	// separador
	// decimal
	// coma
	// (,)

	/**
	 * Returns application request
	 * 
	 * @return
	 */
	protected HttpServletRequest getHttpServletRequest() {
		return ((HttpServletRequest) getExternalContext().getRequest());
	}

	/**
	 * Returns External Context from actual Faces context
	 *
	 * @return
	 */
	protected ExternalContext getExternalContext() {
		return getFacesContext().getExternalContext();
	}

	/**
	 * Obtiene la fecha actual del sistema operativo.
	 * 
	 * @return
	 */
	protected Date getCurrentDate() {
		return new Date();
	}

	/**
	 * Obtiene locate
	 * 
	 * @return
	 */
	protected Locale getLocaleObj() {
		return DEFAULT_LOCALE;
	}

	/**
	 * Obtiene locate
	 * 
	 * @return
	 */
	protected static Locale getLocaleObjStatic() {
		return DEFAULT_LOCALE;
	}

	/**
	 * Params se envia null si no hay parametros
	 *
	 * @param key
	 * @param params
	 * @return
	 */
	protected String getBundleMensajes(String key, Object params[]) {
		Locale locale = getFacesContext().getViewRoot().getLocale();

		ResourceBundle bundle = ResourceBundle.getBundle(
				"ec.gob.secob.seguridad.web.recursos.mensajes", locale,
				getCurrentClassLoader(params));

		String mensaje = bundle.getString(key);

		if (params != null && params.length > 0) {
			MessageFormat mf = new MessageFormat(mensaje, locale);
			mensaje = mf.format(params, new StringBuffer(), null).toString();
		}

		return mensaje;
	}

	/**
	 * Params se envia null si no hay parametros
	 *
	 * @param key
	 * @param params
	 * @return
	 */
	protected String getBundleEtiquetas(String key, Object params[]) {
		Locale locale = DEFAULT_LOCALE;

		if (getFacesContext().getViewRoot() != null) {
			locale = getFacesContext().getViewRoot().getLocale();
		}

		ResourceBundle bundle = ResourceBundle.getBundle(
				"ec.gob.secob.seguridad.web.recursos.etiquetas", locale,
				getCurrentClassLoader(params));

		String mensaje = bundle.getString(key);

		if (params != null && params.length > 0) {
			MessageFormat mf = new MessageFormat(mensaje, locale);
			mensaje = mf.format(params, new StringBuffer(), null).toString();
		}

		return mensaje;
	}

	/**
	 * Returns Jsf actual instance
	 *
	 * @return
	 */
	protected FacesContext getFacesContext() {
		return (FacesContext.getCurrentInstance());
	}

	/**
	 *
	 * @param defaultObject
	 *
	 * @return
	 *
	 */
	protected static ClassLoader getCurrentClassLoader(Object defaultObject) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		if (loader == null) {
			loader = defaultObject.getClass().getClassLoader();
		}

		return loader;
	}
}