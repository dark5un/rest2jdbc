package org.handler.utilities;

import java.io.Serializable;

public class Caster implements Serializable {

	private static final long serialVersionUID = -6835187812153278405L;

	private static Caster _instance; 
	private Caster() { } 
	public static synchronized Caster instance() { 
		if (_instance==null) { 
			_instance = new Caster(); 
		} 
		return _instance; 
	} 

	@SuppressWarnings("unchecked")
    public <T> T safeCast(Object obj, Class<T> type) {
        if (type != null && type.isInstance(obj)) {
            return (T) obj;
        }
        return null;
    }

	public <T> T as(Object obj, Class<T> type) {
		return safeCast(obj, type);
    }
}
