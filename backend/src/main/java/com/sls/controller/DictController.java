package com.sls.controller;

import com.sls.common.ApiResponse;
import com.sls.dto.DictItemVO;
import com.sls.dto.DictTypeVO;
import com.sls.dto.OptionVO;
import com.sls.service.DictService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 字典维护接口（FR-04）
 */
@RestController
@RequestMapping("/api/dicts")
public class DictController {

    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    @GetMapping("/types")
    public ApiResponse<List<DictTypeVO>> types() {
        return ApiResponse.ok(dictService.listTypes());
    }

    @PostMapping("/types")
    public ApiResponse<Void> createType(@RequestBody Map<String, String> body) {
        dictService.createType(body.get("typeCode"), body.get("typeName"));
        return ApiResponse.ok("新增成功", null);
    }

    @PutMapping("/types/{typeCode}")
    public ApiResponse<Void> updateType(@PathVariable String typeCode, @RequestBody Map<String, String> body) {
        dictService.updateType(typeCode, body.get("typeName"));
        return ApiResponse.ok("修改成功", null);
    }

    @DeleteMapping("/types/{typeCode}")
    public ApiResponse<Void> deleteType(@PathVariable String typeCode) {
        dictService.deleteType(typeCode);
        return ApiResponse.ok("删除成功", null);
    }

    @GetMapping("/types/{typeCode}/items")
    public ApiResponse<List<DictItemVO>> items(@PathVariable String typeCode) {
        return ApiResponse.ok(dictService.listItems(typeCode));
    }

    @PostMapping("/types/{typeCode}/items")
    public ApiResponse<Void> createItem(@PathVariable String typeCode, @RequestBody Map<String, String> body) {
        dictService.createItem(typeCode, body.get("itemCode"), body.get("label"));
        return ApiResponse.ok("新增成功", null);
    }

    @PutMapping("/types/{typeCode}/items/{itemCode}")
    public ApiResponse<Void> updateItem(@PathVariable String typeCode, @PathVariable String itemCode,
                                        @RequestBody Map<String, String> body) {
        dictService.updateItem(typeCode, itemCode, body.get("label"), body.get("status"));
        return ApiResponse.ok("修改成功", null);
    }

    @DeleteMapping("/types/{typeCode}/items/{itemCode}")
    public ApiResponse<Void> deleteItem(@PathVariable String typeCode, @PathVariable String itemCode) {
        dictService.deleteItem(typeCode, itemCode);
        return ApiResponse.ok("删除成功", null);
    }

    /** 下拉选项（仅启用项） */
    @GetMapping("/options/{typeCode}")
    public ApiResponse<List<OptionVO>> options(@PathVariable String typeCode) {
        return ApiResponse.ok(dictService.options(typeCode));
    }
}
