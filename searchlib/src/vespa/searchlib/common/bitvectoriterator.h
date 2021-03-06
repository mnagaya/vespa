// Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#pragma once

#include "bitvector.h"
#include <vespa/searchlib/queryeval/searchiterator.h>

namespace search {

namespace fef { class TermFieldMatchDataArray; }
namespace fef { class TermFieldMatchData; }

class BitVectorIterator : public queryeval::SearchIterator
{
protected:
    BitVectorIterator(const BitVector & bv, uint32_t docIdLimit, fef::TermFieldMatchData &matchData);
    void initRange(uint32_t begin, uint32_t end) override;

    uint32_t          _docIdLimit;
    const BitVector & _bv;
private:
    void visitMembers(vespalib::ObjectVisitor &visitor) const override;
    void doUnpack(uint32_t docId) override;
    bool isBitVector() const override { return true; }
    fef::TermFieldMatchData  &_tfmd;
public:
    virtual bool isInverted() const = 0;
    const void *getBitValues() const { return _bv.getStart(); }

    Trinary is_strict() const override { return Trinary::False; }
    uint32_t getDocIdLimit() const { return _docIdLimit; }
    static UP create(const BitVector *const other, fef::TermFieldMatchData &matchData, bool strict, bool inverted = false);
    static UP create(const BitVector *const other, uint32_t docIdLimit,
                     fef::TermFieldMatchData &matchData, bool strict, bool inverted = false);
};

} // namespace search
