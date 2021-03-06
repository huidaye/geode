/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.internal.cache.wan.wancommand;

import static org.apache.geode.distributed.ConfigurationProperties.DISTRIBUTED_SYSTEM_ID;
import static org.apache.geode.distributed.ConfigurationProperties.GROUPS;
import static org.apache.geode.distributed.ConfigurationProperties.REMOTE_LOCATORS;
import static org.apache.geode.internal.cache.wan.wancommand.WANCommandUtils.createReceiver;
import static org.apache.geode.internal.cache.wan.wancommand.WANCommandUtils.getMember;
import static org.apache.geode.internal.cache.wan.wancommand.WANCommandUtils.validateGatewayReceiverMXBeanProxy;
import static org.apache.geode.internal.cache.wan.wancommand.WANCommandUtils.verifyReceiverState;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.management.cli.Result;
import org.apache.geode.management.internal.cli.i18n.CliStrings;
import org.apache.geode.management.internal.cli.result.CommandResult;
import org.apache.geode.management.internal.cli.result.TabularResultData;
import org.apache.geode.test.dunit.rules.ClusterStartupRule;
import org.apache.geode.test.dunit.rules.MemberVM;
import org.apache.geode.test.junit.categories.DistributedTest;
import org.apache.geode.test.junit.categories.WanTest;
import org.apache.geode.test.junit.rules.GfshCommandRule;

@Category({DistributedTest.class, WanTest.class})
@SuppressWarnings("serial")
public class StartGatewayReceiverCommandDUnitTest implements Serializable {

  @Rule
  public ClusterStartupRule clusterStartupRule = new ClusterStartupRule(8);

  @Rule
  public transient GfshCommandRule gfsh = new GfshCommandRule();

  private MemberVM locatorSite1;
  private MemberVM locatorSite2;
  private MemberVM server1;
  private MemberVM server2;
  private MemberVM server3;
  private MemberVM server4;
  private MemberVM server5;

  @Before
  public void before() throws Exception {
    Properties props = new Properties();
    props.setProperty(DISTRIBUTED_SYSTEM_ID, "" + 1);
    locatorSite1 = clusterStartupRule.startLocatorVM(1, props);

    props.setProperty(DISTRIBUTED_SYSTEM_ID, "" + 2);
    props.setProperty(REMOTE_LOCATORS, "localhost[" + locatorSite1.getPort() + "]");
    locatorSite2 = clusterStartupRule.startLocatorVM(2, props);

    // Connect Gfsh to locator.
    gfsh.connectAndVerify(locatorSite1);
  }

  /**
   * Test wan commands for error in input 1> start gateway-sender command needs only one of member
   * or group.
   */
  @Test
  public void testStartGatewayReceiver_ErrorConditions() throws Exception {
    Integer locator1Port = locatorSite1.getPort();

    // setup servers in Site #1
    server1 = clusterStartupRule.startServerVM(3, locator1Port);

    DistributedMember vm1Member = getMember(server1.getVM());
    String command = CliStrings.START_GATEWAYRECEIVER + " --" + CliStrings.MEMBER + "="
        + vm1Member.getId() + " --" + CliStrings.GROUP + "=RG1";
    gfsh.executeAndAssertThat(command).statusIsError()
        .containsOutput(CliStrings.PROVIDE_EITHER_MEMBER_OR_GROUP_MESSAGE);
  }

  @Test
  public void testStartGatewayReceiver() throws Exception {
    Integer locator1Port = locatorSite1.getPort();

    // setup servers in Site #1
    server1 = clusterStartupRule.startServerVM(3, locator1Port);
    server2 = clusterStartupRule.startServerVM(4, locator1Port);
    server3 = clusterStartupRule.startServerVM(5, locator1Port);

    server1.invoke(() -> createReceiver(locator1Port));
    server2.invoke(() -> createReceiver(locator1Port));
    server3.invoke(() -> createReceiver(locator1Port));

    server1.invoke(() -> verifyReceiverState(false));
    server2.invoke(() -> verifyReceiverState(false));
    server3.invoke(() -> verifyReceiverState(false));

    locatorSite1
        .invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server1.getVM()), false));
    locatorSite1
        .invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server2.getVM()), false));
    locatorSite1
        .invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server3.getVM()), false));

    String command = CliStrings.START_GATEWAYRECEIVER;
    CommandResult cmdResult = gfsh.executeCommand(command);
    assertThat(cmdResult).isNotNull();

    TabularResultData resultData = (TabularResultData) cmdResult.getResultData();
    List<String> status = resultData.retrieveAllValues("Result");
    assertThat(status).hasSize(3);
    assertThat(status).doesNotContain("Error");
    assertThat(status).contains("OK");

    locatorSite1.invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server1.getVM()), true));
    locatorSite1.invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server2.getVM()), true));
    locatorSite1.invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server3.getVM()), true));

    server1.invoke(() -> verifyReceiverState(true));
    server2.invoke(() -> verifyReceiverState(true));
    server3.invoke(() -> verifyReceiverState(true));
  }

  /**
   * test to validate that the start gateway sender starts the gateway sender on a member
   */
  @Test
  public void testStartGatewayReceiver_onMember() throws Exception {
    Integer locator1Port = locatorSite1.getPort();

    // setup servers in Site #1
    server1 = clusterStartupRule.startServerVM(3, locator1Port);
    server2 = clusterStartupRule.startServerVM(4, locator1Port);
    server3 = clusterStartupRule.startServerVM(5, locator1Port);

    server1.invoke(() -> createReceiver(locator1Port));
    server2.invoke(() -> createReceiver(locator1Port));
    server3.invoke(() -> createReceiver(locator1Port));

    server1.invoke(() -> verifyReceiverState(false));
    server2.invoke(() -> verifyReceiverState(false));
    server3.invoke(() -> verifyReceiverState(false));

    locatorSite1
        .invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server1.getVM()), false));
    locatorSite1
        .invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server2.getVM()), false));
    locatorSite1
        .invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server3.getVM()), false));

    DistributedMember vm1Member = getMember(server1.getVM());
    String command =
        CliStrings.START_GATEWAYRECEIVER + " --" + CliStrings.MEMBER + "=" + vm1Member.getId();
    CommandResult cmdResult = gfsh.executeCommand(command);
    assertThat(cmdResult).isNotNull();

    String strCmdResult = cmdResult.toString();
    assertThat(cmdResult.getStatus()).isSameAs(Result.Status.OK);
    assertThat(strCmdResult).contains("is started on member");

    locatorSite1.invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server1.getVM()), true));
    locatorSite1
        .invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server2.getVM()), false));
    locatorSite1
        .invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server3.getVM()), false));

    server1.invoke(() -> verifyReceiverState(true));
    server2.invoke(() -> verifyReceiverState(false));
    server3.invoke(() -> verifyReceiverState(false));
  }

  /**
   * test to validate that the start gateway sender starts the gateway sender on a group of members
   */
  @Test
  public void testStartGatewayReceiver_Group() throws Exception {
    Integer locator1Port = locatorSite1.getPort();

    // setup servers in Site #1
    server1 = startServerWithGroups(3, "RG1", locator1Port);
    server2 = startServerWithGroups(4, "RG1", locator1Port);
    server3 = startServerWithGroups(5, "RG1", locator1Port);

    server1.invoke(() -> createReceiver(locator1Port));
    server2.invoke(() -> createReceiver(locator1Port));
    server3.invoke(() -> createReceiver(locator1Port));

    server1.invoke(() -> verifyReceiverState(false));
    server2.invoke(() -> verifyReceiverState(false));
    server3.invoke(() -> verifyReceiverState(false));

    locatorSite1
        .invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server1.getVM()), false));
    locatorSite1
        .invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server2.getVM()), false));
    locatorSite1
        .invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server3.getVM()), false));

    String command = CliStrings.START_GATEWAYRECEIVER + " --" + CliStrings.GROUP + "=RG1";
    CommandResult cmdResult = gfsh.executeCommand(command);
    assertThat(cmdResult).isNotNull();
    assertThat(cmdResult.getStatus()).isSameAs(Result.Status.OK);

    TabularResultData resultData = (TabularResultData) cmdResult.getResultData();
    List<String> status = resultData.retrieveAllValues("Result");
    assertThat(status).hasSize(3);
    assertThat(status).doesNotContain("Error");
    assertThat(status).contains("OK");

    locatorSite1.invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server1.getVM()), true));
    locatorSite1.invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server2.getVM()), true));
    locatorSite1.invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server3.getVM()), true));

    server1.invoke(() -> verifyReceiverState(true));
    server2.invoke(() -> verifyReceiverState(true));
    server3.invoke(() -> verifyReceiverState(true));
  }

  /**
   * Test to validate the scenario gateway sender is started when one or more sender members belongs
   * to multiple groups
   */
  @Test
  public void testStartGatewayReceiver_MultipleGroup() throws Exception {
    Integer locator1Port = locatorSite1.getPort();

    // setup servers in Site #1
    server1 = startServerWithGroups(3, "RG1", locator1Port);
    server2 = startServerWithGroups(4, "RG1", locator1Port);
    server3 = startServerWithGroups(5, "RG1,RG2", locator1Port);
    server4 = startServerWithGroups(6, "RG1,RG2", locator1Port);
    server5 = startServerWithGroups(7, "RG3", locator1Port);

    server1.invoke(() -> createReceiver(locator1Port));
    server2.invoke(() -> createReceiver(locator1Port));
    server3.invoke(() -> createReceiver(locator1Port));
    server4.invoke(() -> createReceiver(locator1Port));
    server5.invoke(() -> createReceiver(locator1Port));

    server1.invoke(() -> verifyReceiverState(false));
    server2.invoke(() -> verifyReceiverState(false));
    server3.invoke(() -> verifyReceiverState(false));
    server4.invoke(() -> verifyReceiverState(false));
    server5.invoke(() -> verifyReceiverState(false));

    locatorSite1
        .invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server1.getVM()), false));
    locatorSite1
        .invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server2.getVM()), false));
    locatorSite1
        .invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server3.getVM()), false));
    locatorSite1
        .invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server4.getVM()), false));
    locatorSite1
        .invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server5.getVM()), false));

    String command = CliStrings.START_GATEWAYRECEIVER + " --" + CliStrings.GROUP + "=RG1,RG2";
    CommandResult cmdResult = gfsh.executeCommand(command);
    assertThat(cmdResult).isNotNull();
    assertThat(cmdResult.getStatus()).isSameAs(Result.Status.OK);

    TabularResultData resultData = (TabularResultData) cmdResult.getResultData();
    List<String> status = resultData.retrieveAllValues("Result");
    assertThat(status).hasSize(4);
    assertThat(status).doesNotContain("Error");
    assertThat(status).contains("OK");

    locatorSite1.invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server1.getVM()), true));
    locatorSite1.invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server2.getVM()), true));
    locatorSite1.invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server3.getVM()), true));
    locatorSite1.invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server4.getVM()), true));
    locatorSite1
        .invoke(() -> validateGatewayReceiverMXBeanProxy(getMember(server5.getVM()), false));

    server1.invoke(() -> verifyReceiverState(true));
    server2.invoke(() -> verifyReceiverState(true));
    server3.invoke(() -> verifyReceiverState(true));
    server4.invoke(() -> verifyReceiverState(true));
    server5.invoke(() -> verifyReceiverState(false));
  }

  private MemberVM startServerWithGroups(int index, String groups, int locPort) throws Exception {
    Properties props = new Properties();
    props.setProperty(GROUPS, groups);
    return clusterStartupRule.startServerVM(index, props, locPort);
  }
}
