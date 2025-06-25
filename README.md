```gestion-association/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── association/
│   │   │           ├── App.java                          # Classe principale du programme
│   │   │           │
│   │   │           ├── config/                           # Configurations générales (ex: DB, sécurité)
│   │   │           │   ├── DatabaseConfig.java
│   │   │           │   ├── PasswordPolicy.java
│   │   │           │   ├── LoginPolicy.java
│   │   │           │   └── SecurityConstants.java
│   │   │           │
│   │   │           ├── dao/                              # DAO interfaces
│   │   │           │   ├── GenericDao.java
│   │   │           │   ├── ObservableDao.java
│   │   │           │   ├── MembreDao.java
│   │   │           │   ├── ContributionDao.java
│   │   │           │   ├── EmpruntDao.java
│   │   │           │   ├── RapportDao.java
│   │   │           │   ├── UtilisateurDao.java
│   │   │           │   └── impl/                         # Implémentations DAO
│   │   │           │       ├── GenericDaoImpl.java
│   │   │           │       ├── MembreDaoImpl.java
│   │   │           │       ├── ContributionDaoImpl.java
│   │   │           │       ├── EmpruntDaoImpl.java
│   │   │           │       ├── RapportDaoImpl.java
│   │   │           │       └── UtilisateurDaoImpl.java
│   │   │           │
│   │   │           ├── exception/                        # Exceptions custom et gestionnaires
│   │   │           │   ├── ApiException.java
│   │   │           │   ├── AuthenticationException.java
│   │   │           │   ├── AuthorizationException.java
│   │   │           │   ├── BusinessException.java
│   │   │           │   ├── DatabaseException.java
│   │   │           │   ├── FileStorageException.java
│   │   │           │   ├── ResourceNotFoundException.java
│   │   │           │   ├── SecurityException.java
│   │   │           │   ├── ValidationException.java
│   │   │           │   ├── dto/                           # DTO liés aux exceptions
│   │   │           │   │   └── ErrorDetails.java
│   │   │           │   └── handler/                       # Handler global d’exceptions
│   │   │           │       └── CustomExceptionHandler.java
│   │   │           │
│   │   │           ├── manager/                          # Managers (services métier)
│   │   │           │   ├── BaseManager.java
│   │   │           │   ├── MembreManager.java
│   │   │           │   ├── ContributionManager.java
│   │   │           │   ├── EmpruntManager.java
│   │   │           │   ├── RapportManager.java
│   │   │           │   ├── SecurityManager.java
│   │   │           │   └── dto/                           # DTO métiers (ex: critères de recherche)
│   │   │           │       ├── LoginRequest.java
│   │   │           │       └── MembreSearchCriteria.java
│   │   │           │
│   │   │           ├── model/                            # Entités et classes métier
│   │   │           │   ├── Entity.java
│   │   │           │   ├── Personne.java
│   │   │           │   ├── Membre.java
│   │   │           │   ├── Rapport.java
│   │   │           │   ├── enums/                         # Enumérations métier
│   │   │           │   │   ├── StatutMembre.java
│   │   │           │   │   ├── TypeContribution.java
│   │   │           │   │   ├── StatutEmprunt.java
│   │   │           │   │   └── TypeRapport.java
│   │   │           │   ├── media/
│   │   │           │   │   └── MediaFile.java
│   │   │           │   └── transaction/
│   │   │           │       ├── Transaction.java
│   │   │           │       ├── Contribution.java
│   │   │           │       └── Emprunt.java
│   │   │           │
│   │   │           ├── security/                         # Sécurité (auth, roles, JWT...)
│   │   │           │   ├── config/
│   │   │           │   │   ├── PasswordPolicy.java
│   │   │           │   │   ├── LoginPolicy.java
│   │   │           │   │   └── SecurityConstants.java
│   │   │           │   ├── exception/
│   │   │           │   │   ├── AuthenticationException.java
│   │   │           │   │   ├── AuthorizationException.java
│   │   │           │   │   └── SecurityException.java
│   │   │           │   ├── model/
│   │   │           │   │   ├── Role.java
│   │   │           │   │   ├── Utilisateur.java
│   │   │           │   │   └── Session.java
│   │   │           │   ├── service/
│   │   │           │   │   ├── AuthenticationService.java
│   │   │           │   │   ├── AuthorizationService.java
│   │   │           │   │   ├── PasswordService.java
│   │   │           │   │   ├── impl/
│   │   │           │   │   │   ├── AuthenticationServiceImpl.java
│   │   │           │   │   │   ├── AuthorizationServiceImpl.java
│   │   │           │   │   │   └── PasswordServiceImpl.java
│   │   │           │   │   └── jwt/
│   │   │           │   │       ├── JwtTokenProvider.java
│   │   │           │   │       └── JwtAuthenticationFilter.java
│   │   │           │
│   │   │           ├── util/                             # Utilitaires divers
│   │   │           │   ├── constants/
│   │   │           │   │   ├── AppConstants.java
│   │   │           │   │   └── DatePattern.java
│   │   │           │   ├── dto/
│   │   │           │   │   └── ApiResponse.java
│   │   │           │   ├── file/
│   │   │           │   │   ├── FileStorageService.java
│   │   │           │   │   ├── FileUtil.java
│   │   │           │   │   └── image/
│   │   │           │   │       └── ImageProcessingUtil.java
│   │   │           │   ├── listener/
│   │   │           │   │   └── AuditListener.java
│   │   │           │   └── utils/
│   │   │           │       ├── DateUtil.java
│   │   │           │       ├── EncryptionUtil.java
│   │   │           │       ├── MoneyUtil.java
│   │   │           │       ├── NotificationUtil.java
│   │   │           │       └── ValidationUtil.java
│   │   │           │
│   │   │           ├── view/                             # Interface graphique Swing, contrôleurs UI, etc.
│   │   │           │   └── (tes classes UI Swing)
│   │   │           │
│   │   └── resources/                                    # Ressources
│   │       ├── application.properties
│   │       ├── images/
│   │       └── sql/
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── association/
│                   └── (tes classes de test)
```

Points clés :
exception/ : tout ce qui concerne les exceptions personnalisées et leur gestion (DTO d’erreur, handler global).
security/ : regroupement clair pour tout ce qui touche la sécurité, y compris exceptions propres à la sécurité.
dao/impl/ : implémentations concrètes des interfaces DAO.
manager/ : services métiers, séparés des DAO.
model/ : entités métier, enums, sous-packages pour transactions, médias, etc.
util/ : utilitaires très variés, bien segmentés en sous-packages.
view/ : interface utilisateur Swing.

````
UI (view/MainPanel) 
     ↓ appelle
Manager (MembreManager)
     ↓ appelle
DAO (MembreDao)
     ↓ exécute requête SQL
Base de données
````