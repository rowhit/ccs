// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.containercluster.dao;

import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import org.springframework.stereotype.Component;

import com.cloud.containercluster.ContainerCluster.Event;
import com.cloud.containercluster.ContainerClusterVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.containercluster.ContainerCluster;

import java.util.List;

@Component
public class ContainerClusterDaoImpl extends GenericDaoBase<ContainerClusterVO, Long> implements ContainerClusterDao {

    private final SearchBuilder<ContainerClusterVO> AccountIdSearch;
    private final SearchBuilder<ContainerClusterVO> GarbageCollectedSearch;
    private final SearchBuilder<ContainerClusterVO> StateSearch;
    private final SearchBuilder<ContainerClusterVO> SameNetworkSearch;

    public ContainerClusterDaoImpl() {
        AccountIdSearch = createSearchBuilder();
        AccountIdSearch.and("account", AccountIdSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AccountIdSearch.done();

        GarbageCollectedSearch = createSearchBuilder();
        GarbageCollectedSearch.and("gc", GarbageCollectedSearch.entity().ischeckForGc(), SearchCriteria.Op.EQ);
        GarbageCollectedSearch.and("state", GarbageCollectedSearch.entity().getState(), SearchCriteria.Op.NEQ);
        GarbageCollectedSearch.done();

        StateSearch = createSearchBuilder();
        StateSearch.and("state", StateSearch.entity().getState(), SearchCriteria.Op.EQ);
        StateSearch.done();

        SameNetworkSearch = createSearchBuilder();
        SameNetworkSearch.and("network_id", SameNetworkSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);
        SameNetworkSearch.done();
    }

    @Override
    public List<ContainerClusterVO> listByAccount(long accountId) {
        SearchCriteria<ContainerClusterVO> sc = AccountIdSearch.create();
        sc.setParameters("account", accountId);
        return listBy(sc, null);
    }

    @Override
    public List<ContainerClusterVO> findContainerClustersToGarbageCollect() {
        SearchCriteria<ContainerClusterVO> sc = GarbageCollectedSearch.create();
        sc.setParameters("gc", true);
        sc.setParameters("state", ContainerCluster.State.Destroying);
        return listBy(sc);
    }

    @Override
    public List<ContainerClusterVO> findContainerClustersInState(ContainerCluster.State state) {
        SearchCriteria<ContainerClusterVO> sc = StateSearch.create();
        sc.setParameters("state", state);
        return listBy(sc);
    }

    @Override
    public boolean updateState(com.cloud.containercluster.ContainerCluster.State currentState, Event event, com.cloud.containercluster.ContainerCluster.State nextState,
            ContainerCluster vo, Object data) {
        // TODO: ensure this update is correct
        TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();

        ContainerClusterVO ccVo = (ContainerClusterVO)vo;
        ccVo.setState(nextState);
        super.update(ccVo.getId(), ccVo);

        txn.commit();
        return true;
    }

    public List<ContainerClusterVO> listByNetworkId(long networkId) {
        SearchCriteria<ContainerClusterVO> sc = SameNetworkSearch.create();
        sc.setParameters("network_id", networkId);
        return this.listBy(sc);
    }
}
