package org.apache.geode.statistics.client.connection

import org.apache.geode.statistics.micrometer.CounterStatisticMeter
import org.apache.geode.statistics.micrometer.GaugeStatisticMeter
import org.apache.geode.statistics.micrometer.MicrometerMeterGroup
import org.apache.geode.statistics.micrometer.TimerStatisticMeter

class PoolStats(val poolName: String) : MicrometerMeterGroup("PoolStats") {

    private val initialContactsMeter = GaugeStatisticMeter("pool.initial.contacts", "Number of contacts initially by user")
    private val locatorsDiscoveredMeter = GaugeStatisticMeter("pool.locators.discovered", "Current number of locators discovered")
    private val serversDiscoveredMeter = GaugeStatisticMeter("pool.servers.discovered", "Current number of servers discovered")
    private val subscriptionServersMeter = GaugeStatisticMeter("pool.servers.subscription", "Number of servers hosting this clients subscriptions")
    private val requestsToLocatorMeter = CounterStatisticMeter("pool.locator.requests", "Number of requests from this connection pool to a locator")
    private val responsesFromLocatorMeter = CounterStatisticMeter("pool.locator.responses", "Number of responses received by pool from locator")
    private val currentConnectionCountMeter = GaugeStatisticMeter("pool.connection.count", "Current number of connections")
    private val currentPoolConnectionCountMeter = GaugeStatisticMeter("pool.connection.pooled.count", "Current number of pool connections")
    private val connectionCreateMeter = CounterStatisticMeter("pool.connection.create", "Total number of times a connection has been created.")
    private val connectionDisconnectMeter = CounterStatisticMeter("pool.connection.disconnect", "Total number of times a connection has been destroyed.")
    private val minPoolConnectCountMeter = CounterStatisticMeter("pool.connection.create.min", "Total number of connects done to maintain minimum pool size.")
    private val loadConditioningConnectCountMeter = CounterStatisticMeter("pool.connection.create.loadconditioning", "Total number of connects done due to load conditioning.")
    private val loadConditioningReplaceCountMeter = CounterStatisticMeter("pool.connection.loadconditioning.replace", "Total number of times a load conditioning connect was done but was not used.")
    private val idleConnectionDisconnectCountMeter = CounterStatisticMeter("pool.connection.disconnect.idle", "Total number of disconnects done due to idle expiration.")
    private val loadConditioningDisconnectCountMeter = CounterStatisticMeter("pool.connection.disconnect.loadconditioning", "Total number of disconnects done due to load conditioning expiration.")
    private val idleConnectionCheckCountMeter = CounterStatisticMeter("pool.connection.check.idle", "Total number of checks done for idle expiration.")
    private val loadConditioningCheckCountMeter = CounterStatisticMeter("pool.connection.check.loadconditioning", "Total number of checks done for load conditioning expiration.")
    private val loadConditioningExtensionCountMeter = CounterStatisticMeter("pool.connection.extension.loadconditioning", "Total number of times a connection's load conditioning has been extended because the servers are still balanced.")
    private val connectionWaitInProgressMeter = GaugeStatisticMeter("pool.connection.wait.inprogress", "Current number of threads waiting for a connection")
    private val connectionWaitMeter = CounterStatisticMeter("pool.connection.wait.count", "Total number of times a thread completed waiting for a connection (by timing out or by getting a connection).")
    private val connectionWaitTimeMeter = TimerStatisticMeter("pool.connection.wait.time", "Total number of nanoseconds spent waiting for a connection.", "nanoseconds")
    private val clientOpsInProgressMeter = GaugeStatisticMeter("pool.connection.ops.inprogress", "Current number of clientOps being executed")
    private val clientOpSendsInProgressMeter = GaugeStatisticMeter("pool.connection.ops.sends.inprogress", "Current number of clientOp sends being executed")
    private val clientOpSendsCountMeter = CounterStatisticMeter("pool.connection.ops.sends.count", "Total number of clientOp sends that have completed successfully")
    private val clientOpSendsFailuresMeter = CounterStatisticMeter("pool.connection.ops.sends.count", "Total number of clientOp sends that have failed")
    private val clientOpSuccessMeter = CounterStatisticMeter("pool.connection.ops.success", "Total number of clientOps completed successfully")
    private val clientOpFailureMeter = CounterStatisticMeter("pool.connection.ops.failure", "Total number of clientOp attempts that have failed")
    private val clientOpTimeoutMeter = CounterStatisticMeter("pool.connection.ops.timeout", "Total number of clientOp attempts that have timed out")
    private val clientOpSendTimeMeter = TimerStatisticMeter("pool.connection.ops.sends.time", "Total amount of time, in nanoseconds spent doing clientOp sends", "nanoseconds")
    private val clientOpTimeMeter = TimerStatisticMeter("pool.connection.ops.time", "Total amount of time, in nanoseconds spent doing clientOps", "nanoseconds")

    override fun initializeStaticMeters() {
        registerMeter(initialContactsMeter)
        registerMeter(locatorsDiscoveredMeter)
        registerMeter(serversDiscoveredMeter)
        registerMeter(subscriptionServersMeter)
        registerMeter(requestsToLocatorMeter)
        registerMeter(responsesFromLocatorMeter)
        registerMeter(currentConnectionCountMeter)
        registerMeter(currentPoolConnectionCountMeter)
        registerMeter(connectionCreateMeter)
        registerMeter(connectionDisconnectMeter)
        registerMeter(minPoolConnectCountMeter)
        registerMeter(loadConditioningConnectCountMeter)
        registerMeter(loadConditioningReplaceCountMeter)
        registerMeter(idleConnectionDisconnectCountMeter)
        registerMeter(loadConditioningDisconnectCountMeter)
        registerMeter(idleConnectionCheckCountMeter)
        registerMeter(loadConditioningCheckCountMeter)
        registerMeter(loadConditioningExtensionCountMeter)
        registerMeter(connectionWaitInProgressMeter)
        registerMeter(connectionWaitMeter)
        registerMeter(connectionWaitTimeMeter)
        registerMeter(clientOpsInProgressMeter)
        registerMeter(clientOpSendsInProgressMeter)
        registerMeter(clientOpSendsCountMeter)
        registerMeter(clientOpSendsFailuresMeter)
        registerMeter(clientOpSuccessMeter)
        registerMeter(clientOpFailureMeter)
        registerMeter(clientOpTimeoutMeter)
        registerMeter(clientOpSendTimeMeter)
        registerMeter(clientOpTimeMeter)
    }

    fun setInitialContacts(value: Int) {
        initialContactsMeter.overrideValue(value.toDouble())
    }

    fun setServerCount(value: Int) {
        serversDiscoveredMeter.overrideValue(value.toDouble())
    }

    fun setSubscriptionCount(value: Int) {
        subscriptionServersMeter.overrideValue(value.toDouble())
    }

    fun setLocatorCount(value: Int) {
        locatorsDiscoveredMeter.overrideValue(value.toDouble())
    }

    fun incLocatorRequests() {
        requestsToLocatorMeter.increment()
    }

    fun incLocatorResponses() {
        responsesFromLocatorMeter.increment()
    }

    fun setLocatorRequests(value: Long) {
        requestsToLocatorMeter.increment(value.toDouble())
    }

    fun setLocatorResponses(value: Long) {
        responsesFromLocatorMeter.increment(value.toDouble())
    }

    fun incConnections(value: Int) {
        currentConnectionCountMeter.increment(value.toDouble())
        if (value > 0) {
            connectionCreateMeter.increment(value.toDouble())
        } else if (value < 0) {
            connectionDisconnectMeter.increment(value.toDouble())
        }
    }

    fun incPoolConnections(value: Int) {
        currentPoolConnectionCountMeter.increment(value.toDouble())
    }

    fun incPrefillConnect() {
        minPoolConnectCountMeter.increment()
    }

    fun incLoadConditioningCheck() {
        loadConditioningCheckCountMeter.increment()
    }

    fun incLoadConditioningExtensions() {
        loadConditioningExtensionCountMeter.increment()
    }

    fun incIdleCheck() {
        idleConnectionCheckCountMeter.increment()
    }

    fun incLoadConditioningConnect() {
        loadConditioningConnectCountMeter.increment()
    }

    fun incLoadConditioningReplaceTimeouts() {
        loadConditioningReplaceCountMeter.increment()
    }

    fun incLoadConditioningDisconnect() {
        loadConditioningDisconnectCountMeter.increment()
    }

    fun incIdleExpire(value: Int) {
        idleConnectionDisconnectCountMeter.increment(value.toDouble())
    }

    fun beginConnectionWait(): Long {
        connectionWaitInProgressMeter.increment()
        return System.nanoTime()
    }

    fun endConnectionWait(start: Long) {
        val duration = System.nanoTime() - start
        connectionWaitInProgressMeter.decrement()
        connectionWaitMeter.increment()
        connectionWaitTimeMeter.recordValue(duration)
    }

    fun startClientOp() {
        clientOpsInProgressMeter.increment()
        clientOpSendsInProgressMeter.increment()
    }

    fun endClientOpSend(duration: Long, failed: Boolean) {
        clientOpSendsInProgressMeter.decrement()
        if (failed) {
            clientOpSendsFailuresMeter.increment()
        } else {
            clientOpSendsCountMeter.increment()
        }
        clientOpSendTimeMeter.recordValue(duration)
    }

    fun endClientOp(duration: Long, timedOut: Boolean, failed: Boolean) {
        clientOpsInProgressMeter.decrement()
        when {
            timedOut -> clientOpTimeoutMeter.increment()
            failed -> clientOpFailureMeter.increment()
            else -> clientOpSuccessMeter.increment()
        }
        clientOpTimeMeter.recordValue(duration)
    }
}