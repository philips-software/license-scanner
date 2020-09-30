/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.persistence;

import org.springframework.data.repository.CrudRepository;

public interface DetectionRepository extends CrudRepository<DetectionEntity, Long> {
}
