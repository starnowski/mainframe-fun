package com.github.starnowski.db2.fun

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = DB2Application)
@ContextConfiguration
class JdbcTemplateItTest extends Specification {

}