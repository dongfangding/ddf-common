/*
 * Copyright 2011-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ddf.boot.common.redis.serializer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Simple {@link String} to {@literal byte[]} (and back) serializer. Converts {@link String Strings}
 * into bytes and vice-versa using the specified charset (by default {@literal UTF-8}).
 * <p>
 * Useful when the interaction with the Redis happens mainly through Strings.
 * <p>
 * Does not perform any {@literal null} conversion since empty strings are valid keys/values.
 *
 * @author Costin Leau
 * @author Christoph Strobl
 * @author Mark Paluch
 */
public class ObjectStringRedisSerializer implements RedisSerializer<Object> {

	private final Charset charset;

	/**
	 * {@link ObjectStringRedisSerializer} to use 7 bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic Latin block of the Unicode
	 * character set.
	 *
	 * @see StandardCharsets#US_ASCII
	 * @since 2.1
	 */
	public static final ObjectStringRedisSerializer US_ASCII = new ObjectStringRedisSerializer(StandardCharsets.US_ASCII);

	/**
	 * {@link ObjectStringRedisSerializer} to use ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1.
	 *
	 * @see StandardCharsets#ISO_8859_1
	 * @since 2.1
	 */
	public static final ObjectStringRedisSerializer ISO_8859_1 = new ObjectStringRedisSerializer(StandardCharsets.ISO_8859_1);

	/**
	 * {@link ObjectStringRedisSerializer} to use 8 bit UCS Transformation Format.
	 *
	 * @see StandardCharsets#UTF_8
	 * @since 2.1
	 */
	public static final ObjectStringRedisSerializer UTF_8 = new ObjectStringRedisSerializer(StandardCharsets.UTF_8);

	/**
	 * Creates a new {@link ObjectStringRedisSerializer} using {@link StandardCharsets#UTF_8 UTF-8}.
	 */
	public ObjectStringRedisSerializer() {
		this(StandardCharsets.UTF_8);
	}

	/**
	 * Creates a new {@link ObjectStringRedisSerializer} using the given {@link Charset} to encode and decode strings.
	 *
	 * @param charset must not be {@literal null}.
	 */
	public ObjectStringRedisSerializer(Charset charset) {

		Assert.notNull(charset, "Charset must not be null!");
		this.charset = charset;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.serializer.RedisSerializer#deserialize(byte[])
	 */
	@Override
	public String deserialize(@Nullable byte[] bytes) {
		return (bytes == null ? null : new String(bytes, charset));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.serializer.RedisSerializer#serialize(java.lang.Object)
	 */
	@Override
	public byte[] serialize(@Nullable Object string) {
		return (string == null ? null : string.toString().getBytes(charset));
	}

	@Override
	public Class<?> getTargetType() {
		return String.class;
	}
}
