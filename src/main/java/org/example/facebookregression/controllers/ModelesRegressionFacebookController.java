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
import org.example.facebookregression.models.ModelesRegressionFacebook;
import org.example.facebookregression.models.dto.ModelesRegressionFacebookSearch;
import org.springframework.web.bind.annotation.*;
import org.example.facebookregression.services.interfaces.ModelesRegressionFacebookService;
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
@RequestMapping("/modelesregressionfacebooks")
@Tag(name = "Modeles regression facebook", description = "Modeles regression facebook Management APIs")
public class ModelesRegressionFacebookController  {
	private final ModelesRegressionFacebookService modelesregressionfacebookService;

	public ModelesRegressionFacebookController(ModelesRegressionFacebookService modelesregressionfacebookService) {
	   this.modelesregressionfacebookService = modelesregressionfacebookService;
	}

	@Operation(
	    summary = "Retrieve all modeles regression facebook",
	    description = "Get a paginated and sorted list of modeles regression facebook items. Returns an empty list if no data is found."
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Successfully retrieved the paginated list of modeles regression facebook",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "204",
	        description = "No modeles regression facebook found"
	    )
	})
	@GetMapping
	public ResponseEntity<RestResponse<Page<ModelesRegressionFacebook>>> getAllModelesRegressionFacebooks(
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
	    Page<ModelesRegressionFacebook> modelesregressionfacebooks = modelesregressionfacebookService.getAllModelesRegressionFacebook(pageable);
	
	    String message = "modeles regression facebook retrieved successfully";
	
	    if(!modelesregressionfacebooks.hasContent()) {
	            message = "No modeles regression facebook found";
	            throw new ResourceNotFoundException(message);
	    }
	
	    RestResponse<Page<ModelesRegressionFacebook>> response = RestResponse.buildSuccessResponse(
	            HttpStatus.OK,
	            message,
	            modelesregressionfacebooks
	    );
	    return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@Operation(
	    summary = "Retrieve all modeles regression facebook",
	    description = "Get a paginated and sorted list of modeles regression facebook items. Returns an empty list if no data is found."
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Successfully retrieved the paginated list of modeles regression facebook",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "204",
	        description = "No modeles regression facebook found"
	    )
	})
	@PostMapping("/search")
	public ResponseEntity<RestResponse<Page<ModelesRegressionFacebook>>> getAllModelesRegressionFacebooks(
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
	    @RequestBody ModelesRegressionFacebookSearch object) {
	
	    Sort sortObj = WebUtils.createSortObject(sortParam);
	    Pageable pageable = PageRequest.of(page, size, sortObj);
	    Page<ModelesRegressionFacebook> modelesregressionfacebooks = modelesregressionfacebookService.getAllModelesRegressionFacebook(pageable, object);
	
	    String message = "modeles regression facebook retrieved successfully";
	
	    if(!modelesregressionfacebooks.hasContent()) {
	            message = "No modeles regression facebook found";
	            throw new ResourceNotFoundException(message);
	    }
	
	    RestResponse<Page<ModelesRegressionFacebook>> response = RestResponse.buildSuccessResponse(
	            HttpStatus.OK,
	            message,
	            modelesregressionfacebooks
	    );
	    return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@Operation(
	    summary = "Get modeles regression facebook by ID",
	    description = "Retrieve a specific modeles regression facebook item by its ID"
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Successfully retrieved the modeles regression facebook",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "404",
	        description = "Modeles regression facebook not found with the provided ID"
	    )
	})
	@GetMapping("/{id}")
	public ResponseEntity<RestResponse<ModelesRegressionFacebook>> getModelesRegressionFacebookById(
	    @Parameter(description = "ID of the modeles regression facebook to retrieve", required = true)
	    @PathVariable Integer id
	) {
	    ModelesRegressionFacebook modelesregressionfacebook = modelesregressionfacebookService.getModelesRegressionFacebookById(id);
	    RestResponse<ModelesRegressionFacebook> response = RestResponse.buildSuccessResponse(HttpStatus.OK,
	            "modeles regression facebook retrieved successfully", modelesregressionfacebook);
	    return ResponseEntity.ok(response);
	}
	@Operation(
	     summary = "Export Modeles regression facebook to CSV",
	     description = "Generate a CSV file from a provided list of Modeles regression facebook objects."
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
	public ResponseEntity<byte[]> exportModelesRegressionFacebookToCsv(@RequestBody List<ModelesRegressionFacebook> modelesregressionfacebook) {
	  
	  String csvContent = modelesregressionfacebookService.exportModelesRegressionFacebookToCSV(modelesregressionfacebook);
	  
	  return ResponseEntity.ok()
	   .header("Content-Disposition", "attachment; filename=data.csv")
	   .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
	   .body(csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}
 

	@Operation(
	    summary = "Create new modeles regression facebook",
 
	    description = "Create a new modeles regression facebook item with the provided information"
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "201",
	        description = "Modeles regression facebook created successfully",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "400",
	        description = "Invalid modeles regression facebook supplied"
	    )
	})
	@PostMapping
	public ResponseEntity<?> createModelesRegressionFacebook(
	    @Parameter(description = "Modeles regression facebook object to be created", required = true)
	    @RequestBody @Valid ModelesRegressionFacebook modelesregressionfacebook
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
	    ModelesRegressionFacebook newModelesRegressionFacebook = modelesregressionfacebookService.createModelesRegressionFacebook(modelesregressionfacebook);
	    RestResponse<ModelesRegressionFacebook> response = RestResponse.buildSuccessResponse(HttpStatus.CREATED,
	            "modeles regression facebook created successfully", newModelesRegressionFacebook);
	    return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

 
	@Operation(
	    summary = "Update existing modeles regression facebook",

	    description = "Update an existing modeles regression facebook item with the provided information"
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Modeles regression facebook updated successfully",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "404",
	        description = "Modeles regression facebook not found with the provided ID"
	    ),
	    @ApiResponse(
	        responseCode = "400",
	        description = "Invalid modeles regression facebook supplied"
	    )
	})
	@PutMapping("/{id}")
	public ResponseEntity<?> updateModelesRegressionFacebook(
	    @Parameter(description = "ID of the modeles regression facebook to update", required = true)
	    @PathVariable Integer id,
	    @Parameter(description = "Updated modeles regression facebook object", required = true)
	    @RequestBody @Valid ModelesRegressionFacebook modelesregressionfacebook
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
	    ModelesRegressionFacebook updateModelesRegressionFacebook = modelesregressionfacebookService.updateModelesRegressionFacebook(id, modelesregressionfacebook);
	    RestResponse<ModelesRegressionFacebook> response = RestResponse.buildSuccessResponse(HttpStatus.OK,
	            "modeles regression facebook updated successfully", updateModelesRegressionFacebook);
	    return ResponseEntity.ok(response);
	}


	@Operation(
	    summary = "Delete modeles regression facebook",

	    description = "Delete a modeles regression facebook item by its ID. Returns success even if modeles regression facebook was already deleted."
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Modeles regression facebook deleted successfully or already deleted"
	    )
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<RestResponse<Void>> deleteModelesRegressionFacebookById(
	    @Parameter(description = "ID of the modeles regression facebook to delete", required = true)
	    @PathVariable Integer id
	) {
		try {
			modelesregressionfacebookService.getModelesRegressionFacebookById(id);
			modelesregressionfacebookService.deleteModelesRegressionFacebook(id);
			return ResponseEntity.ok(
				RestResponse.buildSuccessResponse(
					HttpStatus.OK,
					"modeles regression facebook deleted successfully",
					null
				)
			);
		}catch (ResourceNotFoundException ex) {
			return ResponseEntity.ok(
				RestResponse.buildSuccessResponse(
					HttpStatus.OK,
					"modeles regression facebook already deleted or does not exist",
					null
				)
			);
		}
	}



}
