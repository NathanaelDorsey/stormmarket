---
story_id: "e02s02"
epic_id: "e02"
title: "ProductService stock callers handle Result"
status: "pending"
estimate: "S"
---

## Story: ProductService stock callers handle Result

**Vertical Slice:** Updates `ProductService` (and any other direct callers of ProductRepository stock methods) to handle the new `Result<Integer, StockError>` return types instead of checking `int == 0`.

### Acceptance Criteria
- [ ] `ProductService.reserveStock` (if exists) or any service method calling `productRepository.reserveStock` handles `Result<Integer, StockError>`
- [ ] `ProductService.deductStock` (if exists) or any service method calling `productRepository.deductStock` handles `Result<Integer, StockError>`
- [ ] `ProductService.captureStock` (if exists) or any service method calling `productRepository.captureStock` handles `Result<Integer, StockError>`
- [ ] `ProductService.restoreStock` / `releaseReservedStock` callers handle `Result<Void, StockError>`
- [ ] On `Err`, propagate the `StockError` upward (return `Result.err(stockError)` or throw if not yet converted)
- [ ] On `Ok`, continue with the returned row count
- [ ] No behavioral changes — only error handling style changes

### Technical Notes
- Check `ProductService.java` for any direct calls to ProductRepository stock methods
- Currently OrderService calls these directly (not through ProductService), so ProductService may have few/no callers
- This story ensures any existing callers in ProductService are updated
- Use pattern matching: `if (result.isOk()) { int rows = ((Ok<?, ?>) result).value(); } else { return Result.err(((Err<?, ?>) result).error()); }`
- Or use a helper method if pattern emerges

### Files to Modify
- `src/main/java/com/nathanaeldorsey/stormmarket/product/ProductService.java` (if it calls stock methods)

### Definition of Done
- Code compiles
- Follows project code style
- All ProductService callers of stock methods handle Result correctly