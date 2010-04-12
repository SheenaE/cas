/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.view;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

import org.jasig.cas.TestUtils;
import org.jasig.cas.server.authentication.AttributePrincipal;
import org.jasig.cas.server.authentication.Authentication;
import org.jasig.cas.validation.ImmutableAssertionImpl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 */
public class Cas10ResponseViewTests extends TestCase {

    private final Cas10ResponseView view = new Cas10ResponseView();

    private Map<String, Object> model;

    protected void setUp() throws Exception {
        this.model = new HashMap<String,Object>();
        List<Authentication> list = new ArrayList<Authentication>();
        list.add(new Authentication() {
            public Date getAuthenticationDate() {
                return new Date();
            }

            public Map<String, List<Object>> getAuthenticationMetaData() {
                return Collections.emptyMap();
            }

            public AttributePrincipal getPrincipal() {
                return new AttributePrincipal() {
                    public List<Object> getAttributeValues(String attribute) {
                        return null;
                    }

                    public Object getAttributeValue(String attribute) {
                        return null;
                    }

                    public Map<String, List<Object>> getAttributes() {
                        return Collections.emptyMap();
                    }

                    public String getName() {
                        return "test";
                    }
                };
            }

            public boolean isLongTermAuthentication() {
                return false;
            }

            public String getAuthenticationMethod() {
                return "foo";
            }
        });
        this.model.put("assertion", new ImmutableAssertionImpl(list,
            TestUtils.getService("TestService"), true));
    }

    public void testSuccessView() throws Exception {
        final MockWriterHttpMockHttpServletResponse response = new MockWriterHttpMockHttpServletResponse();
        this.view.setSuccessResponse(true);
        this.view.render(this.model, new MockHttpServletRequest(), response
            );
        assertEquals("yes\ntest\n", response.getWrittenValue());
    }

    public void testFailureView() throws Exception {
        final MockWriterHttpMockHttpServletResponse response = new MockWriterHttpMockHttpServletResponse();
        this.view.setSuccessResponse(false);
        this.view.render(this.model, new MockHttpServletRequest(),
            response);
        assertEquals("no\n\n", response.getWrittenValue());
    }

    protected static class MockWriterHttpMockHttpServletResponse extends
        MockHttpServletResponse {
        
        private StringBuilder builder = new StringBuilder();

        public PrintWriter getWriter() {
            try {
                return new MockPrintWriter(new ByteArrayOutputStream(), this.builder);
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }
        
        public String getWrittenValue() {
            return this.builder.toString();
        }
    }

    protected static class MockPrintWriter extends PrintWriter {
        
        final StringBuilder builder;

        public MockPrintWriter(OutputStream out, boolean autoFlush, final StringBuilder builder) {
            super(out, autoFlush);
            this.builder = builder;
        }

        public MockPrintWriter(OutputStream out, final StringBuilder builder) {
            super(out);
            this.builder = builder;
        }

        public MockPrintWriter(Writer out, boolean autoFlush, final StringBuilder builder) {
            super(out, autoFlush);
            this.builder = builder;
        }

        public MockPrintWriter(Writer out, final StringBuilder builder) {
            super(out);
            this.builder = builder;
        }

        public void print(String s) {
            this.builder.append(s);
        }
    }
}
