															BUT: 
															
Tenter d'acc�lerer les d�veloppements en fournissant des raccourcis permettant de g�n�rer du code en tapant le d�but 
d'un mot cl� suivi de ctrl+espace (un peu comme syso + ctrl + espace pour faire un System.out.println (...))



														INSTALLATION :
														
- importer les templates Java (templates_java.xml) via Window / Preferences / Java / Editor / Templates / Import... 
- importer les templates Maven (templates_maven.xml) Window / Preferences / Maven / Templates / Import..
Ils contiennent un nom de raccourcis et une description (qui commence toujours pas custom_ ) 



												RACCOURCIS JAVA DISPONIBLES :

ccc + ctrl + espace  (pour custom comment code, ne fonctionne qu'� l'int�rieur d'une m�thode) :

/* ================== *
 *                      
 * ================== */

										~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

ccm + ctrl + espace :

    /* ======================================================= *
     *                      code m�tier 
     * ======================================================= */


										~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

cco + ctrl + espace :

    /* ======================================================= *
     *                      constructeurs
     * ======================================================= */


										~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

cgs + ctrl + espace (pour comment getter setter) :

    /* ======================================================= *
     *                      getters & setters 
     * ======================================================= */


										~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

cinherit + ctrl + espace  (h�ritage d'un commentaire de la m�thode de la classe m�re, pour que Sonar les interpr�te) :

/** {@inheritDoc} */
 

										~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


logger + ctrl + espace (ne fonctionne qu'en tant que variable globale) :
final Logger LOGGER = LoggerFactory.getLogger(getClass());


										~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

cequalshash  :

    /* ======================================================= *
     *                      equals / hash code
     * ======================================================= */

										~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

cjsfvalidation  :

FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, ${cursor}, null);
										

												RACCOURCIS MAVEN DISPONIBLES :

C�t� maven, pour g�n�rer une nouvelle d�pendance, il faut aller dans le bloc <dependencies> et faire ctrl + espace et choisir 
custom_new_dependency dans le menu d�roulant. Ca affichera : 

		<dependency>
			<groupId>groupId</groupId>   
			<artifactId>artifactId</artifactId>
			<version>version</version>
		</dependency>

Avec le curseur positionn� � gauche de "groupId"
