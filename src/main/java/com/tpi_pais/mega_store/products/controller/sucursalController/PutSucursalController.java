package com.tpi_pais.mega_store.products.controller.sucursalController;
import com.tpi_pais.mega_store.products.dto.SucursalDTO;
import com.tpi_pais.mega_store.products.model.Sucursal;
import com.tpi_pais.mega_store.products.service.ISucursalService;
import com.tpi_pais.mega_store.utils.ApiResponse;
import com.tpi_pais.mega_store.utils.ExpresionesRegulares;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/products")
public class PutSucursalController {
    @Autowired
    private ISucursalService modelService;

    @PutMapping("/sucursal")
    public ResponseEntity<?> actualizar(@RequestBody SucursalDTO model){
        /*
         * Validaciones:
         * 1) Que se haya enviado un SucursalDTO
         *   En caso que falle se ejecuta el @ExceptionHandler
         * 2) Que el dto enviado tenga un id distinto de null o 0
         *   En caso que falle se retorna una badrequest
         * 3) Que el id enviado corresponda a un objeto Sucursal y que el mismo no este eliminado
         *   En caso que falle se retorna una badrequest
         * 4) Que el dto enviado tenga un nombre distinto de null o ""
         *   En caso que falle se retorna una badrequest
         * 5) En caso de que contenga un nombre verifico si coincide con la expresion regular determinada.
         *   Las condiciones son:
         *   - Debe estar formado solo por letras y/o espacios.
         *   - Puede contener espacios, pero solo entre las palabras, no al principio ni al final.
         *   - Puede contener 1 y solo 1 espacio entre 2 palabras.
         * Una vez pasado esto se debe capitalizar el nombre para estandarizar todas las sucursals.
         * 6) Que el nuevo nombre no este registrado en otro objeto Sucursal
         *   En caso que falle se retorna una badrequest
         * */

        try{
            Sucursal sucursalModificar = modelService.buscarPorId(model.getId());
            if (sucursalModificar == null){
                ApiResponse<Object> response = new ApiResponse<>(
                        404,
                        "Error: Not Found.",
                        null,
                        "El id no corresponde a ninguna sucursal, se debe enviar el id de una sucursal existente."
                );
                return ResponseEntity.badRequest().body(response);
            } else {
                if (sucursalModificar.esEliminado()){
                    ApiResponse<Object> response = new ApiResponse<>(
                            400,
                            "Error: Bad Request.",
                            null,
                            "La sucursal no se puede modificar debido a que se encuentra eliminada."
                    );
                    return ResponseEntity.badRequest().body(response);
                }
            }

            if (model.noTieneNombre()) {
                ApiResponse<Object> response = new ApiResponse<>(
                        400,
                        "Error: Bad Request.",
                        null,
                        "La sucursal debe tener un nombre."
                );
                return ResponseEntity.badRequest().body(response);
            };
            ExpresionesRegulares expReg = new ExpresionesRegulares();
            if (!expReg.verificarCaracteres(model.getNombre())){
                ApiResponse<Object> response = new ApiResponse<>(
                        400,
                        "Error: Bad Request.",
                        null,
                        "El nombre debe estar formado únicamente por letras y números."
                );
                return ResponseEntity.badRequest().body(response);
            }
            if (!expReg.verificarTextoConEspacios(model.getNombre())){
                model.setNombre(expReg.corregirCadena(model.getNombre()));
                if (model.getNombre() == ""){
                    ApiResponse<Object> response = new ApiResponse<>(
                            400,
                            "Error: Bad Request.",
                            null,
                            "El nombre debe estar formado unicamente por letras y numeros."
                    );
                    return ResponseEntity.badRequest().body(response);
                }
            }
            model.capitalizarNombre();
            Sucursal aux = modelService.buscarPorNombre(model.getNombre());
            if (aux != null){
                ApiResponse<Object> response = new ApiResponse<>(
                        400,
                        "Error: Bad Request.",
                        null,
                        "Ya existe una sucursal con este nombre, no pueden haber 2 sucursals con el mismo nombre."
                );
                return ResponseEntity.badRequest().body(response);
            }
            SucursalDTO modelGuardado = modelService.guardar(model);
            ApiResponse<Object> response = new ApiResponse<>(
                    201,
                    "Created.",
                    modelGuardado,
                    null
            );
            return ResponseEntity.ok().body(response);

        }catch (Exception e){
            ApiResponse<Object> response = new ApiResponse<>(
                    400,
                    "Error: Error inesperado.",
                    null,
                    ""+e
            );
            return ResponseEntity.badRequest().body(response);
        }

    }
    @PutMapping("/sucursal/recuperar/{id}")
    public ResponseEntity<?> recuperar(@PathVariable Integer id) {
        /*
         * Validaciones:
         * 1) Que el id se haya enviado.
         *   En caso que falle se ejecuta el @ExceptionHandler
         * 2) Que el id sea un entero.
         *   En caso que falle se ejecuta el @ExceptionHandler
         * 3) Que exista una sucursal con dicho id.
         *   Se realiza la busqueda del obj y si el mismo retorna null se devuelve el badrequest
         * 4) Que la sucursal encontrada este eliminada.
         *   Si se encuentra la sucursal, y la misma no esta elimianda se retorna un badrequest.
         * En caso de que pase todas las verificacioens se cambia el la fechaEliminacion por el valor null.
         * */
        try {
            Sucursal model = modelService.buscarPorId(id);
            if (model == null) {
                ApiResponse<Object> response = new ApiResponse<>(
                        404,
                        "Error: Not Found.",
                        null,
                        "El id no corresponde a ninguna sucursal, se debe enviar el id de una sucursal existente.."
                );
                return ResponseEntity.badRequest().body(response);
            }
            if (model.getFechaEliminacion() == null) {
                ApiResponse<Object> response = new ApiResponse<>(
                        400,
                        "Error: Bad Request.",
                        null,
                        "La sucursal ya no se encuentra eliminada, se debe enviar el id de una sucursal eliminada."
                );
                return ResponseEntity.badRequest().body(response);
            }
            model.recuperar();
            modelService.recuperar(model);
            ApiResponse<Object> response = new ApiResponse<>(
                    200,
                    "OK.",
                    model,
                    null
            );
            return ResponseEntity.ok().body(response);
        } catch (Exception e){
            ApiResponse<Object> response = new ApiResponse<>(
                    400,
                    "Error: Error inesperado.",
                    null,
                    ""+e
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Manejador de excepciones para cuando el parámetro no es del tipo esperado (ej. no es un entero)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        // Creamos una respuesta en formato JSON con el error
        String error = String.format("El parámetro '%s' debe ser un número entero válido.", ex.getName());
        ApiResponse<Object> response = new ApiResponse<>(
                400,
                "Error de tipo de argumento",
                null,
                error
        );

        return ResponseEntity.badRequest().body(response);
    }
}
