package com.elias.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ToString
public class EliasException extends RuntimeException {
	private static final long serialVersionUID = -6749288051114679515L;
	private int code = 500;

	public EliasException(String msg) {
		super(msg);
	}

	public EliasException(int code, String msg) {
		super(msg);
		this.code = code;
	}

}
