/**********************************************************************************************
 Copyright 2019 Sridhar Paladugu

 Permission is hereby granted, free of charge, to any person obtaining a copy of this 
 software and associated documentation files (the "Software"), to deal in the Software 
 without restriction, including without limitation the rights to use, copy, modify, 
 merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
 permit persons to whom the Software is furnished to do so, subject to the following 
 conditions:

 The above copyright notice and this permission notice shall be included in all copies 
 or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
 OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 OTHER DEALINGS IN THE SOFTWARE.
 *********************************************************************************************/

package io.pivotal.rtsmadlib.workflow.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author Sridhar Paladugu
 *
 */
public class MADlibflowControlErrorHandler implements ResponseErrorHandler {

	/**
	 * 
	 */
	public MADlibflowControlErrorHandler() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.client.ResponseErrorHandler#hasError(org.
	 * springframework.http.client.ClientHttpResponse)
	 */
	@Override
	public boolean hasError(ClientHttpResponse response) throws IOException {
		if (response.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.client.ResponseErrorHandler#handleError(org.
	 * springframework.http.client.ClientHttpResponse)
	 */
	@Override
	public void handleError(ClientHttpResponse response) throws IOException {
		if (response.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
			InputStream is = response.getBody();
			String body = new BufferedReader(new InputStreamReader(is)).lines()
						.collect(Collectors.joining("\n"));
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, body);
		}

	}

}
