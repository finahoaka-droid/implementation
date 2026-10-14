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
import org.example.facebookregression.models.DonneesFacebookEncoded;
import org.example.facebookregression.models.dto.DonneesFacebookEncodedSearch;
import org.springframework.web.bind.annotation.*;
import org.example.facebookregression.services.interfaces.DonneesFacebookEncodedService;
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
@RequestMapping("/donneesfacebookencodeds")
@Tag(name = "Donnees facebook encoded", description = "Donnees facebook encoded Management APIs")
public class DonneesFacebookEncodedController  {
	private final DonneesFacebookEncodedService donneesfacebookencodedService;

	public DonneesFacebookEncodedController(DonneesFacebookEncodedService donneesfacebookencodedService) {
	   this.donneesfacebookencodedService = donneesfacebookencodedService;
	}

	@Operation(
	    summary = "Retrieve all donnees facebook encoded",
	    description = "Get a paginated and sorted list of donnees facebook encoded items. Returns an empty list if no data is found."
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Successfully retrieved the paginated list of donnees facebook encoded",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "204",
	        description = "No donnees facebook encoded found"
	    )
	})
	@GetMapping
	public ResponseEntity<RestResponse<Page<DonneesFacebookEncoded>>> getAllDonneesFacebookEncodeds(
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
	    Page<DonneesFacebookEncoded> donneesfacebookencodeds = donneesfacebookencodedService.getAllDonneesFacebookEncoded(pageable);
	
	    String message = "donnees facebook encoded retrieved successfully";
	
	    if(!donneesfacebookencodeds.hasContent()) {
	            message = "No donnees facebook encoded found";
	            throw new ResourceNotFoundException(message);
	    }
	
	    RestResponse<Page<DonneesFacebookEncoded>> response = RestResponse.buildSuccessResponse(
	            HttpStatus.OK,
	            message,
	            donneesfacebookencodeds
	    );
	    return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@Operation(
	    summary = "Retrieve all donnees facebook encoded",
	    description = "Get a paginated and sorted list of donnees facebook encoded items. Returns an empty list if no data is found."
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Successfully retrieved the paginated list of donnees facebook encoded",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "204",
	        description = "No donnees facebook encoded found"
	    )
	})
	@PostMapping("/search")
	public ResponseEntity<RestResponse<Page<DonneesFacebookEncoded>>> getAllDonneesFacebookEncodeds(
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
	    @RequestBody DonneesFacebookEncodedSearch object) {
	
	    Sort sortObj = WebUtils.createSortObject(sortParam);
	    Pageable pageable = PageRequest.of(page, size, sortObj);
	    Page<DonneesFacebookEncoded> donneesfacebookencodeds = donneesfacebookencodedService.getAllDonneesFacebookEncoded(pageable, object);
	
	    String message = "donnees facebook encoded retrieved successfully";
	
	    if(!donneesfacebookencodeds.hasContent()) {
	            message = "No donnees facebook encoded found";
	            throw new ResourceNotFoundException(message);
	    }
	
	    RestResponse<Page<DonneesFacebookEncoded>> response = RestResponse.buildSuccessResponse(
	            HttpStatus.OK,
	            message,
	            donneesfacebookencodeds
	    );
	    return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@Operation(
	    summary = "Get donnees facebook encoded by ID",
	    description = "Retrieve a specific donnees facebook encoded item by its ID"
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Successfully retrieved the donnees facebook encoded",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "404",
	        description = "Donnees facebook encoded not found with the provided ID"
	    )
	})
	@GetMapping("/{id}")
	public ResponseEntity<RestResponse<DonneesFacebookEncoded>> getDonneesFacebookEncodedById(
	    @Parameter(description = "ID of the donnees facebook encoded to retrieve", required = true)
	    @PathVariable Integer id
	) {
	    DonneesFacebookEncoded donneesfacebookencoded = donneesfacebookencodedService.getDonneesFacebookEncodedById(id);
	    RestResponse<DonneesFacebookEncoded> response = RestResponse.buildSuccessResponse(HttpStatus.OK,
	            "donnees facebook encoded retrieved successfully", donneesfacebookencoded);
	    return ResponseEntity.ok(response);
	}
	@Operation(
	     summary = "Export Donnees facebook encoded to CSV",
	     description = "Generate a CSV file from a provided list of Donnees facebook encoded objects."
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
	public ResponseEntity<byte[]> exportDonneesFacebookEncodedToCsv(@RequestBody List<DonneesFacebookEncoded> donneesfacebookencoded) {
	  
	  String csvContent = donneesfacebookencodedService.exportDonneesFacebookEncodedToCSV(donneesfacebookencoded);
	  
	  return ResponseEntity.ok()
	   .header("Content-Disposition", "attachment; filename=data.csv")
	   .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
	   .body(csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}
 

	@Operation(
	    summary = "Create new donnees facebook encoded",
 
	    description = "Create a new donnees facebook encoded item with the provided information"
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "201",
	        description = "Donnees facebook encoded created successfully",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "400",
	        description = "Invalid donnees facebook encoded supplied"
	    )
	})
	@PostMapping
	public ResponseEntity<?> createDonneesFacebookEncoded(
	    @Parameter(description = "Donnees facebook encoded object to be created", required = true)
	    @RequestBody @Valid DonneesFacebookEncoded donneesfacebookencoded
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
	    DonneesFacebookEncoded newDonneesFacebookEncoded = donneesfacebookencodedService.createDonneesFacebookEncoded(donneesfacebookencoded);
	    RestResponse<DonneesFacebookEncoded> response = RestResponse.buildSuccessResponse(HttpStatus.CREATED,
	            "donnees facebook encoded created successfully", newDonneesFacebookEncoded);
	    return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

 
	@Operation(
	    summary = "Update existing donnees facebook encoded",

	    description = "Update an existing donnees facebook encoded item with the provided information"
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Donnees facebook encoded updated successfully",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "404",
	        description = "Donnees facebook encoded not found with the provided ID"
	    ),
	    @ApiResponse(
	        responseCode = "400",
	        description = "Invalid donnees facebook encoded supplied"
	    )
	})
	@PutMapping("/{id}")
	public ResponseEntity<?> updateDonneesFacebookEncoded(
	    @Parameter(description = "ID of the donnees facebook encoded to update", required = true)
	    @PathVariable Integer id,
	    @Parameter(description = "Updated donnees facebook encoded object", required = true)
	    @RequestBody @Valid DonneesFacebookEncoded donneesfacebookencoded
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
	    DonneesFacebookEncoded updateDonneesFacebookEncoded = donneesfacebookencodedService.updateDonneesFacebookEncoded(id, donneesfacebookencoded);
	    RestResponse<DonneesFacebookEncoded> response = RestResponse.buildSuccessResponse(HttpStatus.OK,
	            "donnees facebook encoded updated successfully", updateDonneesFacebookEncoded);
	    return ResponseEntity.ok(response);
	}


	@Operation(
	    summary = "Delete donnees facebook encoded",

	    description = "Delete a donnees facebook encoded item by its ID. Returns success even if donnees facebook encoded was already deleted."
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Donnees facebook encoded deleted successfully or already deleted"
	    )
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<RestResponse<Void>> deleteDonneesFacebookEncodedById(
	    @Parameter(description = "ID of the donnees facebook encoded to delete", required = true)
	    @PathVariable Integer id
	) {
		try {
			donneesfacebookencodedService.getDonneesFacebookEncodedById(id);
			donneesfacebookencodedService.deleteDonneesFacebookEncoded(id);
			return ResponseEntity.ok(
				RestResponse.buildSuccessResponse(
					HttpStatus.OK,
					"donnees facebook encoded deleted successfully",
					null
				)
			);
		}catch (ResourceNotFoundException ex) {
			return ResponseEntity.ok(
				RestResponse.buildSuccessResponse(
					HttpStatus.OK,
					"donnees facebook encoded already deleted or does not exist",
					null
				)
			);
		}
	}



}
