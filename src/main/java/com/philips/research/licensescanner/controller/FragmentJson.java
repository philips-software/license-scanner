package com.philips.research.licensescanner.controller;

import com.philips.research.licensescanner.core.LicenseService;

import java.util.List;

class FragmentJson {
    final String file;
    final int first;
    final int start;
    final int end;
    final List<String> lines;

    FragmentJson(LicenseService.FileFragmentDto dto) {
        this.file = dto.filename;
        this.first = dto.firstLine;
        this.start = dto.focusStart;
        this.end = dto.focusEnd;
        this.lines = dto.lines;
    }
}