package com.github.starnowski.db2.fun

import jakarta.xml.bind.DatatypeConverter
import org.apache.commons.codec.digest.DigestUtils
import spock.lang.Specification

import java.security.MessageDigest

class MD5ChecksumCalculationTest extends Specification {

    def "should calculate correct md5 checksum for file #file"() {
        given:
            def content = getClass().getResourceAsStream(file).readAllBytes()

        when:
            String checksumGenerateByFirstStrategy = ChecksumMD5Utils.calculateMD5ChecksumForByteArrayWithFirstStrategy(content)
            String checksumGenerateBySecondStrategy = ChecksumMD5Utils.calculateMD5ChecksumForByteArrayWithSecondStrategy(content)
            String checksumGenerateByThirdStrategy = ChecksumMD5Utils.calculateMD5ChecksumForByteArrayWithThirdStrategy(content)
            String checksumGenerateByFourthStrategy = ChecksumMD5Utils.calculateMD5ChecksumForByteArrayWithFourthStrategy(content)

        then:
            checksumGenerateByFirstStrategy == checksumGenerateBySecondStrategy
            checksumGenerateByFirstStrategy == checksumGenerateByFourthStrategy
//            checksumGenerateBySecondStrategy == checksumGenerateByThirdStrategy

        where:
            file << ["test1.txt", "test2.txt"]
    }


}