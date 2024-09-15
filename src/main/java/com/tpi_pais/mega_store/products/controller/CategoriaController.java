package com.tpi_pais.mega_store.products.controller;

import com.tpi_pais.mega_store.products.dto.CategoriaDTO;
import com.tpi_pais.mega_store.products.mapper.CategoriaMapper;
import com.tpi_pais.mega_store.exception.RecursoNoEncontradoExcepcion;
import com.tpi_pais.mega_store.products.model.Categoria;
import com.tpi_pais.mega_store.products.service.ICategoriaService;
import com.tpi_pais.mega_store.utils.ApiResponse;
import com.tpi_pais.mega_store.utils.ExpresionesRegulares;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestController
@RequestMapping("mega_store/backend")
@CrossOrigin(value="http://localhost:8080/")
public class CategoriaController {
    @Autowired
    private ICategoriaService modelService;

    @GetMapping({"/categorias"})
    public List<CategoriaDTO> getAll() {
        return modelService.listar();
    }

    @GetMapping("/categoria/{id}")
    public ResponseEntity<?> getPorId(@PathVariable Integer id){
        /*
        * Validaciones:
        * 1) Que el id se haya enviado.
        *   En caso que falle se ejecuta el @ExceptionHandler
        * 2) Que el id sea un entero.
        *   En caso que falle se ejecuta el @ExceptionHandler
        * 3) Que exista una categoria con dicho id.
        *   Se realiza la busqueda del obj y si el mismo retorna null se devuelve el badrequest
        * 4) Que la categoria encontrada no este eliminada.
        *   Si se encuentra la categoria, y la misma esta elimianda se retorna un badrequest.
        * En caso de que pase todas las verificacioens devuelve el recurso encontrado.
        * */

        try {
            Categoria model = modelService.buscarPorId(id);

            if (model == null) {
                ApiResponse<Object> response = new ApiResponse<>(
                        400,
                        "Error: No se encontró la categoría con el ID.",
                        null,
                        "El ID debe corresponder a un objeto de tipo Categoria."
                );
                return ResponseEntity.badRequest().body(response);
            }

            if (model.esEliminado()) {
                ApiResponse<Object> response = new ApiResponse<>(
                        400,
                        "Error: No se puede mostrar debido a que el recurso se encuentra eliminado.",
                        null,
                        "No se puede traer un objeto que este eliminado."
                );
                return ResponseEntity.badRequest().body(response);
            }

            CategoriaDTO modelDTO = CategoriaMapper.toDTO(model);
            ApiResponse<Object> response = new ApiResponse<>(
                    200,
                    "Solicitud Exitosa",
                    modelDTO,
                    null
            );
            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            ApiResponse<Object> response = new ApiResponse<>(
                    400,
                    "Error: Error inesperado.",
                    null,
                    ""+e
            );
            return ResponseEntity.badRequest().body(response);
        }

    }


    
    @PostMapping("/categoria")
    public ResponseEntity<?> guardar(@RequestBody CategoriaDTO model){
        /*
        * Validaciones:
        * 1) Que se haya enviado un CategoriaDTO
        *   En caso que falle se ejecuta el @ExceptionHandler
        * 2) Que el dto enviado tenga un nombre distinto de null o ""
        *   En caso que falle se retorna una badrequest
        * 3) En caso de que contenga un nombre verifico si coincide con la expresion regular determinada.
        *   Las condiciones son:
        *   - Debe estar formado solo por letras y/o espacios.
        *   - Puede contener espacios, pero solo entre las palabras, no al principio ni al final.
        *   - Puede contener 1 y solo 1 espacio entre 2 palabras.
        * Una vez pasado esto se debe capitalizar el nombre para estandarizar todas las categorias.
        * 4) Que no exista una categoria con el nombre.
        *
        * */
        try {
            if (model.noTieneNombre()) {
                ApiResponse<Object> response = new ApiResponse<>(
                        400,
                        "Error: No se envio un nombre.",
                        null,
                        "La categoria debe tener un nombre."
                );
                return ResponseEntity.badRequest().body(response);
            };
            ExpresionesRegulares expReg = new ExpresionesRegulares();

            if (!expReg.verificarTextoConEspacios(model.getNombre())){
                ApiResponse<Object> response = new ApiResponse<>(
                        400,
                        "Error: El nombre no tiene el formato correspondiente.",
                        null,
                        "El nombre debe estar formado unicamente por letras."
                );
                return ResponseEntity.badRequest().body(response);
            }
            model.capitalizarNombre();
            Categoria aux = modelService.buscarPorNombre(model.getNombre());
            if (aux != null){
                ApiResponse<Object> response = new ApiResponse<>(
                        400,
                        "Error: Ya existe una categoria con este nombre.",
                        null,
                        "No pueden haber 2 categorias con el mismo nombre."
                );
                return ResponseEntity.badRequest().body(response);
            }
            CategoriaDTO modelGuardado = modelService.guardar(model);
            ApiResponse<Object> response = new ApiResponse<>(
                    200,
                    "Solicitud Exitosa.",
                    modelGuardado,
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

    @PutMapping("/categoria")
    public ResponseEntity<?> actualizar(@RequestBody CategoriaDTO model){
        /*
        * Validaciones:
        * 1) Que se haya enviado un CategoriaDTO
        *   En caso que falle se ejecuta el @ExceptionHandler
        * 2) Que el dto enviado tenga un id distinto de null o 0
        *   En caso que falle se retorna una badrequest
        * 3) Que el id enviado corresponda a un objeto Categoria y que el mismo no este eliminado
        *   En caso que falle se retorna una badrequest
        * 4) Que el dto enviado tenga un nombre distinto de null o ""
        *   En caso que falle se retorna una badrequest
        * 5) En caso de que contenga un nombre verifico si coincide con la expresion regular determinada.
        *   Las condiciones son:
        *   - Debe estar formado solo por letras y/o espacios.
        *   - Puede contener espacios, pero solo entre las palabras, no al principio ni al final.
        *   - Puede contener 1 y solo 1 espacio entre 2 palabras.
        * Una vez pasado esto se debe capitalizar el nombre para estandarizar todas las categorias.
        * 6) Que el nuevo nombre no este registrado en otro objeto Categoria
        *   En caso que falle se retorna una badrequest
        * */
        //return modelService.guardar(model);
        try{
            Categoria categoriaModificar = modelService.buscarPorId(model.getId());
            if (categoriaModificar == null){
                ApiResponse<Object> response = new ApiResponse<>(
                        400,
                        "Error: El id no corresponde a ninguna categoria.",
                        null,
                        "Se debe enviar el id de una categoria existente."
                );
                return ResponseEntity.badRequest().body(response);
            } else {
                if (categoriaModificar.esEliminado()){
                    ApiResponse<Object> response = new ApiResponse<>(
                            400,
                            "Error: La categoria no se puede modificar debido a que se encuentra elimianda.",
                            null,
                            "Solo se pueden modificar categorias que no esten eliminadas."
                    );
                    return ResponseEntity.badRequest().body(response);
                }
            }

            if (model.noTieneNombre()) {
                ApiResponse<Object> response = new ApiResponse<>(
                        400,
                        "Error: No se envio un nombre.",
                        null,
                        "La categoria debe tener un nombre."
                );
                return ResponseEntity.badRequest().body(response);
            };
            ExpresionesRegulares expReg = new ExpresionesRegulares();

            if (!expReg.verificarTextoConEspacios(model.getNombre())){
                ApiResponse<Object> response = new ApiResponse<>(
                        400,
                        "Error: El nombre no tiene el formato correspondiente.",
                        null,
                        "El nombre debe estar formado unicamente por letras."
                );
                return ResponseEntity.badRequest().body(response);
            }
            model.capitalizarNombre();
            Categoria aux = modelService.buscarPorNombre(model.getNombre());
            if (aux != null){
                ApiResponse<Object> response = new ApiResponse<>(
                        400,
                        "Error: Ya existe una categoria con este nombre.",
                        null,
                        "No pueden haber 2 categorias con el mismo nombre."
                );
                return ResponseEntity.badRequest().body(response);
            }
            CategoriaDTO modelGuardado = modelService.guardar(model);
            ApiResponse<Object> response = new ApiResponse<>(
                    200,
                    "Solicitud Exitosa.",
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

    @DeleteMapping("/categoria/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        /*
         * Validaciones:
         * 1) Que el id se haya enviado.
         *   En caso que falle se ejecuta el @ExceptionHandler
         * 2) Que el id sea un entero.
         *   En caso que falle se ejecuta el @ExceptionHandler
         * 3) Que exista una categoria con dicho id.
         *   Se realiza la busqueda del obj y si el mismo retorna null se devuelve el badrequest
         * 4) Que la categoria encontrada no este eliminada.
         *   Si se encuentra la categoria, y la misma esta elimianda se retorna un badrequest.
         * En caso de que pase todas las verificacioens se cambia el la fechaEliminacion por el valor actual de tiempo.
         * */
        try {
            Categoria model = modelService.buscarPorId(id);
            if (model == null) {
                ApiResponse<Object> response = new ApiResponse<>(
                        400,
                        "Error: El id no corresponde a ninguna categoria.",
                        null,
                        "Se debe enviar el id de una categoria existente."
                );
                return ResponseEntity.badRequest().body(response);
            }
            if (model.getFechaEliminacion() != null) {
                ApiResponse<Object> response = new ApiResponse<>(
                        400,
                        "Error: La categoria ya se encuentra eliminada.",
                        null,
                        "Se debe enviar el id de una categoria no eliminada."
                );
                return ResponseEntity.badRequest().body(response);
            }
            model.eliminar();
            modelService.eliminar(model);
            ApiResponse<Object> response = new ApiResponse<>(
                    200,
                    "Solicitud Exitosa.",
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

    @PutMapping("/categoria/recuperar/{id}")
    public ResponseEntity<?> recuperar(@PathVariable Integer id) {
        /*
         * Validaciones:
         * 1) Que el id se haya enviado.
         *   En caso que falle se ejecuta el @ExceptionHandler
         * 2) Que el id sea un entero.
         *   En caso que falle se ejecuta el @ExceptionHandler
         * 3) Que exista una categoria con dicho id.
         *   Se realiza la busqueda del obj y si el mismo retorna null se devuelve el badrequest
         * 4) Que la categoria encontrada este eliminada.
         *   Si se encuentra la categoria, y la misma no esta elimianda se retorna un badrequest.
         * En caso de que pase todas las verificacioens se cambia el la fechaEliminacion por el valor null.
         * */
        try {
            Categoria model = modelService.buscarPorId(id);
            if (model == null) {
                ApiResponse<Object> response = new ApiResponse<>(
                        400,
                        "Error: El id no corresponde a ninguna categoria.",
                        null,
                        "Se debe enviar el id de una categoria existente."
                );
                return ResponseEntity.badRequest().body(response);
            }
            if (model.getFechaEliminacion() == null) {
                ApiResponse<Object> response = new ApiResponse<>(
                        400,
                        "Error: La categoria ya no se encuentra eliminada.",
                        null,
                        "Se debe enviar el id de una categoria eliminada."
                );
                return ResponseEntity.badRequest().body(response);
            }
            model.recuperar();
            modelService.recuperar(model);
            ApiResponse<Object> response = new ApiResponse<>(
                    200,
                    "Solicitud Exitosa.",
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
                200,
                "Error de tipo de argumento",
                null,
                error
        );

        return ResponseEntity.badRequest().body(response);
    }
}

