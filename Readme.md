# Project on creating REST API with focus on layered approach and separation of concerns.

**Major Features in the Code**

> Separate three major layers: controller, service, and repository.

> Uses inmemory db, so no need to setup any database.

> Separating input from API and data for storage in form of DTO and Entity.

> Field names in DTO and Entity are intentionally different to show mapping and error field mapping.

> Transforming DTO into Entity, and vice versa using ModelMapper.

> Validation on DTO and sending field level errors.

> Validation on Entity and generating field level errors.

> Transforming validation errors of Entity fields into corresponding fields of DTO.

> Throwing appropriate exceptions from service and handling in global exception handler.

> API Docs using springdocs, provides swagger-ui.












