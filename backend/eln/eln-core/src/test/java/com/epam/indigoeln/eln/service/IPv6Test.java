package com.epam.indigoeln.eln.service;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class IPv6Test {

    @Test
    @Disabled("No IPv6 in AWS CodeBuild")
    void testIpv6() throws Exception {
        String preferIPv6 = System.getProperty("java.net.preferIPv6Addresses");
        String preferIPv4 = System.getProperty("java.net.preferIPv4Stack");
        InetAddress[] addresses = InetAddress.getAllByName("pubchem.ncbi.nlm.nih.gov");
        System.out.println("preferIPv4: " + preferIPv4 + ", preferIPv6: " + preferIPv6 + ", addresses: " + Arrays.toString(addresses));
        assertThat(preferIPv6).describedAs("preferIPv4").isEqualTo("true");
        assertThat(preferIPv4).describedAs("preferIPv6").isEqualTo("false");
        assertThat(addresses[0]).describedAs(Arrays.toString(addresses)).isInstanceOf(Inet6Address.class);
    }
}
