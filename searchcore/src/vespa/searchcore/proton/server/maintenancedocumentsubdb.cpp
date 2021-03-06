// Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#include "maintenancedocumentsubdb.h"

namespace proton {

MaintenanceDocumentSubDB::MaintenanceDocumentSubDB()
    : _name(""),
      _sub_db_id(0),
      _meta_store(nullptr),
      _retriever(nullptr),
      _feed_view(nullptr)
{
}

MaintenanceDocumentSubDB::~MaintenanceDocumentSubDB() = default;

MaintenanceDocumentSubDB::MaintenanceDocumentSubDB(const vespalib::string& name,
                                                   uint32_t sub_db_id,
                                                   IDocumentMetaStore::SP meta_store,
                                                   IDocumentRetriever::SP retriever,
                                                   IFeedView::SP feed_view)
    : _name(name),
      _sub_db_id(sub_db_id),
      _meta_store(std::move(meta_store)),
      _retriever(std::move(retriever)),
      _feed_view(std::move(feed_view))
{
}

void
MaintenanceDocumentSubDB::clear()
{
    _name = "";
    _sub_db_id = 0;
    _meta_store.reset();
    _retriever.reset();
    _feed_view.reset();
}

}
