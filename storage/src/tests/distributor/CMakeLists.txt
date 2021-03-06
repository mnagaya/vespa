# Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
vespa_add_library(storage_testdistributor TEST
    SOURCES
    blockingoperationstartertest.cpp
    bucketdbmetricupdatertest.cpp
    bucketgctimecalculatortest.cpp
    bucketstateoperationtest.cpp
    distributor_host_info_reporter_test.cpp
    distributortest.cpp
    distributortestutil.cpp
    externaloperationhandlertest.cpp
    garbagecollectiontest.cpp
    getoperationtest.cpp
    idealstatemanagertest.cpp
    joinbuckettest.cpp
    maintenanceschedulertest.cpp
    mergelimitertest.cpp
    mergeoperationtest.cpp
    messagesenderstub.cpp
    nodeinfotest.cpp
    nodemaintenancestatstrackertest.cpp
    operation_sequencer_test.cpp
    operationtargetresolvertest.cpp
    ownership_transfer_safe_time_point_calculator_test.cpp
    pendingmessagetrackertest.cpp
    persistence_metrics_set_test.cpp
    removebucketoperationtest.cpp
    removelocationtest.cpp
    removeoperationtest.cpp
    simplebucketprioritydatabasetest.cpp
    simplemaintenancescannertest.cpp
    splitbuckettest.cpp
    statecheckerstest.cpp
    statoperationtest.cpp
    statusreporterdelegatetest.cpp
    throttlingoperationstartertest.cpp
    twophaseupdateoperationtest.cpp
    updateoperationtest.cpp
    visitoroperationtest.cpp
    DEPENDS
    storage_distributor
    storage_testcommon
    storage_testhostreporter
)

vespa_add_executable(storage_distributor_gtest_runner_app TEST
    SOURCES
    btree_bucket_database_test.cpp
    bucketdatabasetest.cpp
    bucketdbupdatertest.cpp
    mapbucketdatabasetest.cpp
    putoperationtest.cpp
    # TODO: Depend on storage_testdistributor when all tests have been migrated
    # Fixture etc. dupes with non-gtest runner :
    distributortestutil.cpp
    bucket_db_prune_elision_test.cpp
    messagesenderstub.cpp
    gtest_runner.cpp
    DEPENDS
    storage_distributor
    storage_testcommon
    storage_testhostreporter
    gtest
)

vespa_add_test(
    NAME storage_distributor_gtest_runner_app
    COMMAND storage_distributor_gtest_runner_app
)
