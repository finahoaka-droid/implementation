package org.example.facebookregression.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.example.facebookregression.models.DonneesFacebookBrute;
import org.example.facebookregression.models.dto.DonneesFacebookBruteSearch;
import org.springframework.web.bind.annotation.*;
import org.example.facebookregression.services.interfaces.DonneesFacebookBruteService;
import org.example.facebookregression.utils.WebUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.validation.BindingResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.HashMap;

import org.example.facebookregression.dto.RestResponse;
import org.example.facebookregression.exception.ResourceNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/donneesfacebookbrutes")
@Tag(name = "Donnees facebook brute", description = "Donnees facebook brute Management APIs")
public class DonneesFacebookBruteController  {
	private final DonneesFacebookBruteService donneesfacebookbruteService;

	public DonneesFacebookBruteController(DonneesFacebookBruteService donneesfacebookbruteService) {
	   this.donneesfacebookbruteService = donneesfacebookbruteService;
	}

	@Operation(
	    summary = "Retrieve all donnees facebook brute",
	    description = "Get a paginated and sorted list of donnees facebook brute items. Returns an empty list if no data is found."
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Successfully retrieved the paginated list of donnees facebook brute",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "204",
	        description = "No donnees facebook brute found"
	    )
	})
	@GetMapping
	public ResponseEntity<RestResponse<Page<DonneesFacebookBrute>>> getAllDonneesFacebookBrutes(
	    @Parameter(description = "Page number (0-indexed)", example = "0")
	    @RequestParam(defaultValue = "0") int page,
	
	    @Parameter(description = "Size of the page", example = "10")
	    @RequestParam(defaultValue = "10") int size,
	
	    @Parameter(
	        description = "Sorting criteria in the format: property,(asc|desc). "
	                + "Multiple sort criteria can be passed using semicolon separator. "
	                + "Example: id,asc;name,desc",
	        example = "id,asc"
	    )
	    @RequestParam(defaultValue = "id,asc") String sortParam) {
	
	    Sort sortObj = WebUtils.createSortObject(sortParam);
	    Pageable pageable = PageRequest.of(page, size, sortObj);
	    Page<DonneesFacebookBrute> donneesfacebookbrutes = donneesfacebookbruteService.getAllDonneesFacebookBrute(pageable);
	
	    String message = "donnees facebook brute retrieved successfully";
	
	    if(!donneesfacebookbrutes.hasContent()) {
	            message = "No donnees facebook brute found";
	            throw new ResourceNotFoundException(message);
	    }
	
	    RestResponse<Page<DonneesFacebookBrute>> response = RestResponse.buildSuccessResponse(
	            HttpStatus.OK,
	            message,
	            donneesfacebookbrutes
	    );
	    return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@Operation(
	    summary = "Retrieve all donnees facebook brute",
	    description = "Get a paginated and sorted list of donnees facebook brute items. Returns an empty list if no data is found."
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Successfully retrieved the paginated list of donnees facebook brute",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "204",
	        description = "No donnees facebook brute found"
	    )
	})
	@PostMapping("/search")
	public ResponseEntity<RestResponse<Page<DonneesFacebookBrute>>> getAllDonneesFacebookBrutes(
	    @Parameter(description = "Page number (0-indexed)", example = "0")
	    @RequestParam(defaultValue = "0") int page,
	
	    @Parameter(description = "Size of the page", example = "10")
	    @RequestParam(defaultValue = "10") int size,
	
	    @Parameter(
	        description = "Sorting criteria in the format: property,(asc|desc). "
	                + "Multiple sort criteria can be passed using semicolon separator. "
	                + "Example: id,asc;name,desc",
	        example = "id,asc"
	    )
	    @RequestParam(defaultValue = "id,asc") String sortParam,
	    @RequestBody DonneesFacebookBruteSearch object) {
	
	    Sort sortObj = WebUtils.createSortObject(sortParam);
	    Pageable pageable = PageRequest.of(page, size, sortObj);
	    Page<DonneesFacebookBrute> donneesfacebookbrutes = donneesfacebookbruteService.getAllDonneesFacebookBrute(pageable, object);
	
	    String message = "donnees facebook brute retrieved successfully";
	
	    if(!donneesfacebookbrutes.hasContent()) {
	            message = "No donnees facebook brute found";
	            throw new ResourceNotFoundException(message);
	    }
	
	    RestResponse<Page<DonneesFacebookBrute>> response = RestResponse.buildSuccessResponse(
	            HttpStatus.OK,
	            message,
	            donneesfacebookbrutes
	    );
	    return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@Operation(
	    summary = "Get donnees facebook brute by ID",
	    description = "Retrieve a specific donnees facebook brute item by its ID"
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Successfully retrieved the donnees facebook brute",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "404",
	        description = "Donnees facebook brute not found with the provided ID"
	    )
	})
	@GetMapping("/{id}")
	public ResponseEntity<RestResponse<DonneesFacebookBrute>> getDonneesFacebookBruteById(
	    @Parameter(description = "ID of the donnees facebook brute to retrieve", required = true)
	    @PathVariable Integer id
	) {
	    DonneesFacebookBrute donneesfacebookbrute = donneesfacebookbruteService.getDonneesFacebookBruteById(id);
	    RestResponse<DonneesFacebookBrute> response = RestResponse.buildSuccessResponse(HttpStatus.OK,
	            "donnees facebook brute retrieved successfully", donneesfacebookbrute);
	    return ResponseEntity.ok(response);
	}
	@Operation(
	     summary = "Export Donnees facebook brute to CSV",
	     description = "Generate a CSV file from a provided list of Donnees facebook brute objects."
	)
	@ApiResponses({
	   @ApiResponse(
	   responseCode = "200",
	   description = "CSV file successfully generated",
	   content = @Content(mediaType = "text/csv")
	   ),
	   @ApiResponse(
	   responseCode = "400",
	   description = "Invalid or empty list provided"
	   )
	})
	@PostMapping(value = "/export/csv", consumes = "application/json", produces = "text/csv")
	public ResponseEntity<byte[]> exportDonneesFacebookBruteToCsv(@RequestBody List<DonneesFacebookBrute> donneesfacebookbrute) {
	  
	  String csvContent = donneesfacebookbruteService.exportDonneesFacebookBruteToCSV(donneesfacebookbrute);
	  
	  return ResponseEntity.ok()
	   .header("Content-Disposition", "attachment; filename=data.csv")
	   .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
	   .body(csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}
 

	@Operation(
	    summary = "Create new donnees facebook brute",
 
	    description = "Create a new donnees facebook brute item with the provided information"
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "201",
	        description = "Donnees facebook brute created successfully",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "400",
	        description = "Invalid donnees facebook brute supplied"
	    )
	})
	@PostMapping
	public ResponseEntity<?> createDonneesFacebookBrute(
	    @Parameter(description = "Donnees facebook brute object to be created", required = true)
	    @RequestBody @Valid DonneesFacebookBrute donneesfacebookbrute
	    ,BindingResult bindingResult
	) {
	    if (bindingResult.hasErrors()) {
	       HashMap<String, String> errors = new HashMap<>();
	       bindingResult.getFieldErrors().forEach(error -> {
	           errors.put(error.getField(), error.getDefaultMessage());
	       });
	       RestResponse<HashMap<String, String>> errorResponse =
	       RestResponse.buildErrorResponse(HttpStatus.BAD_REQUEST,
	       "Validation failed", errors);
	
	       return ResponseEntity.badRequest().body(errorResponse);
	    }
	    DonneesFacebookBrute newDonneesFacebookBrute = donneesfacebookbruteService.createDonneesFacebookBrute(donneesfacebookbrute);
	    RestResponse<DonneesFacebookBrute> response = RestResponse.buildSuccessResponse(HttpStatus.CREATED,
	            "donnees facebook brute created successfully", newDonneesFacebookBrute);
	    return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

 
	@Operation(
	    summary = "Update existing donnees facebook brute",

	    description = "Update an existing donnees facebook brute item with the provided information"
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Donnees facebook brute updated successfully",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "404",
	        description = "Donnees facebook brute not found with the provided ID"
	    ),
	    @ApiResponse(
	        responseCode = "400",
	        description = "Invalid donnees facebook brute supplied"
	    )
	})
	@PutMapping("/{id}")
	public ResponseEntity<?> updateDonneesFacebookBrute(
	    @Parameter(description = "ID of the donnees facebook brute to update", required = true)
	    @PathVariable Integer id,
	    @Parameter(description = "Updated donnees facebook brute object", required = true)
	    @RequestBody @Valid DonneesFacebookBrute donneesfacebookbrute
	    ,BindingResult bindingResult
	) {
	    if (bindingResult.hasErrors()) {
	       HashMap<String, String> errors = new HashMap<>();
	       bindingResult.getFieldErrors().forEach(error -> {
	           errors.put(error.getField(), error.getDefaultMessage());
	       });
	       RestResponse<HashMap<String, String>> errorResponse =
	       RestResponse.buildErrorResponse(HttpStatus.BAD_REQUEST,
	       "Validation failed", errors);
	
	       return ResponseEntity.badRequest().body(errorResponse);
	    }
	    DonneesFacebookBrute updateDonneesFacebookBrute = donneesfacebookbruteService.updateDonneesFacebookBrute(id, donneesfacebookbrute);
	    RestResponse<DonneesFacebookBrute> response = RestResponse.buildSuccessResponse(HttpStatus.OK,
	            "donnees facebook brute updated successfully", updateDonneesFacebookBrute);
	    return ResponseEntity.ok(response);
	}


	@Operation(
	    summary = "Delete donnees facebook brute",

	    description = "Delete a donnees facebook brute item by its ID. Returns success even if donnees facebook brute was already deleted."
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Donnees facebook brute deleted successfully or already deleted"
	    )
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<RestResponse<Void>> deleteDonneesFacebookBruteById(
	    @Parameter(description = "ID of the donnees facebook brute to delete", required = true)
	    @PathVariable Integer id
	) {
		try {
			donneesfacebookbruteService.getDonneesFacebookBruteById(id);
			donneesfacebookbruteService.deleteDonneesFacebookBrute(id);
			return ResponseEntity.ok(
				RestResponse.buildSuccessResponse(
					HttpStatus.OK,
					"donnees facebook brute deleted successfully",
					null
				)
			);
		}catch (ResourceNotFoundException ex) {
			return ResponseEntity.ok(
				RestResponse.buildSuccessResponse(
					HttpStatus.OK,
					"donnees facebook brute already deleted or does not exist",
					null
				)
			);
		}
	}



}
