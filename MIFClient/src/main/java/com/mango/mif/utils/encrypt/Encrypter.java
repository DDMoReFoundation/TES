/*******************************************************************************
 * Copyright (C) 2016 Mango Business Solutions Ltd, http://www.mango-solutions.com
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
 *******************************************************************************/
package com.mango.mif.utils.encrypt;

/**
 * interface implemented by different encrypting strategies 
 */
public interface Encrypter {

    /**
     * Encrypts string
     * @param message a plain text message to be encrypted
     * @return an encrypted string representing the input message  
     * @throws EncryptionException if the message could not be encrypted
     */
    public String encrypt(String message) throws EncryptionException;
    
    /**
     * Decrypts string
     * @param message the encrypted message to decrypt
     * @return a plain text string decrypted using the input message
     * @throws EncryptionException if the message could not be decrypted
     */
    public String decrypt(String message) throws EncryptionException;
}
