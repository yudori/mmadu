package com.mmadu.security;

import com.mmadu.security.models.MmaduQualified;
import com.mmadu.security.models.MmaduQualifiedBean;
import com.mmadu.security.providers.MmaduJwtAuthenticationConverter;
import com.mmadu.security.providers.MmaduMethodSecurityExpressionHandler;
import com.mmadu.security.providers.MmaduWebSecurityExpressionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

@Configuration
public class MmaduSecurityAutoConfiguration {
    private String publicKey;

    @Value("${mmadu.identity.public-key:30820122300d06092a864886f70d01010105000382010f003082010a0282010100a31b559ff90788f92436bb61ad0d528cf088a9190a97c7dbf98539409663d54d3fb4a089ccd77ca49165d8d5a76b21b30fc733a558569647498b182dc4a06ea7fd1022b761877c9776d0db5107b8f3e0c67ba0f101315be5989d0a33c6a431a3de07c071457672c6266a1e89d079222c42031ca7c3b563a913c41eee45d20ddaf58a92014adbc4bbe135c055c604380b649b3178540fc4a0c2f7c46aa90f62422d5ae621332bacf6771f2319ae03936fdaf346abdc599e3b63cae4c0d4d8ec5832f1c61b5e370005c3aed880130513970b79de6d5734aa11dcf8fb866ea9d0cfafd55d0c4f27a13763f1d193ca402c5c8a65e198a7500b3e9928552e19a40d3d0203010001}")
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @MmaduQualifiedBean
    public KeyFactory mmaduKeyFactory() throws Exception {
        return KeyFactory.getInstance("RSA");
    }

    @MmaduQualifiedBean
    public RSAPublicKey rsaPublicKey(@MmaduQualified KeyFactory keyFactory) throws Exception {
        X509EncodedKeySpec pubKeySpec
                = new X509EncodedKeySpec(Hex.decode(publicKey));
        return (RSAPublicKey) keyFactory.generatePublic(pubKeySpec);
    }

    @MmaduQualifiedBean
    public JwtDecoder jwtDecoder(@MmaduQualified RSAPublicKey publicKey) {
        return NimbusJwtDecoder.withPublicKey(publicKey)
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
    }

    @MmaduQualifiedBean
    public MmaduJwtAuthenticationConverter mmaduJwtAuthenticationConverter() {
        return new MmaduJwtAuthenticationConverter();
    }

    @MmaduQualifiedBean
    public MmaduWebSecurityExpressionHandler mmaduWebSecurityExpressionHandler() {
        return new MmaduWebSecurityExpressionHandler();
    }

    @MmaduQualifiedBean
    public MmaduMethodSecurityExpressionHandler mmaduMethodSecurityExpressionHandler() {
        return new MmaduMethodSecurityExpressionHandler();
    }

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    static class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
        private MmaduMethodSecurityExpressionHandler expressionHandler;

        @Autowired
        public void setExpressionHandler(MmaduMethodSecurityExpressionHandler expressionHandler) {
            this.expressionHandler = expressionHandler;
        }

        @Override
        protected MethodSecurityExpressionHandler createExpressionHandler() {
            return expressionHandler;
        }
    }
}
