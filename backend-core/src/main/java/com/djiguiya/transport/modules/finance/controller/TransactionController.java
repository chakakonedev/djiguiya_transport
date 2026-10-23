package com.djiguiya.transport.modules.finance.controller;

import com.djiguiya.transport.modules.finance.dto.TransactionRequest;
import com.djiguiya.transport.modules.finance.dto.TransactionResponse;
import com.djiguiya.transport.modules.finance.service.TransactionService;
import com.djiguiya.transport.shared.api.ApiResponse;
import com.djiguiya.transport.shared.security.AppRoles;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CRUD API for ledger transactions (revenue and expense rows). Mutations require ledger write role.
 */
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

	private final TransactionService transactionService;

	/** Lists all non-deleted transactions, newest transaction date first. */
	@GetMapping
	public ApiResponse<List<TransactionResponse>> list() {
		return ApiResponse.ok(transactionService.listAll());
	}

	/** Returns a single active (non-deleted) transaction by id. */
	@GetMapping("/{id}")
	public ApiResponse<TransactionResponse> get(@PathVariable String id) {
		return ApiResponse.ok(transactionService.getById(id));
	}

	/** Creates a ledger row after revenue/expense business validation. */
	@PostMapping
	@PreAuthorize(AppRoles.LEDGER_WRITE)
	public ApiResponse<TransactionResponse> create(@Valid @RequestBody TransactionRequest request) {
		return ApiResponse.ok("transaction.created", transactionService.create(request));
	}

	/** Replaces mutable fields on an existing transaction. */
	@PutMapping("/{id}")
	@PreAuthorize(AppRoles.LEDGER_WRITE)
	public ApiResponse<TransactionResponse> update(
			@PathVariable String id,
			@Valid @RequestBody TransactionRequest request) {
		return ApiResponse.ok("transaction.updated", transactionService.update(id, request));
	}

	/** Soft-deletes a transaction by setting {@code deletedAt}. */
	@DeleteMapping("/{id}")
	@PreAuthorize(AppRoles.LEDGER_WRITE)
	public ApiResponse<Void> delete(@PathVariable String id) {
		transactionService.delete(id);
		return ApiResponse.ok("transaction.deleted", null);
	}
}
