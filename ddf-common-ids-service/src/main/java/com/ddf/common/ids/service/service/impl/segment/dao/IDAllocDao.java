package com.ddf.common.ids.service.service.impl.segment.dao;

import com.ddf.common.ids.service.service.impl.segment.model.LeafAlloc;
import java.util.List;

public interface IDAllocDao {
     List<LeafAlloc> getAllLeafAllocs();
     LeafAlloc updateMaxIdAndGetLeafAlloc(String tag);
     LeafAlloc updateMaxIdByCustomStepAndGetLeafAlloc(LeafAlloc leafAlloc);
     List<String> getAllTags();
}
