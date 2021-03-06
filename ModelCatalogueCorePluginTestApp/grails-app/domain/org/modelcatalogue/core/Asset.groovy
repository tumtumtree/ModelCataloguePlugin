package org.modelcatalogue.core

class Asset extends CatalogueElement {

    Long    size
    String  contentType
    String  originalFileName
    String  md5

    static constraints = {
        contentType maxSize: 255, nullable: true
        md5 maxSize: 32, nullable: true
        originalFileName maxSize: 255, nullable: true
        size nullable: true
    }

    static relationships = [
            incoming: [attachment: 'isAttachedTo']
    ]

    @Override
    Map<CatalogueElement, Object> manualDeleteRelationships(DataModel toBeDeleted) {
        // Asset has no relationships
        return [:]
    }

    static Asset getWithRetries(Serializable id, int attempts = 10) {
        for (int i = 0; i < attempts ; i++) {
            Asset found = Asset.get(id)
            if (found) {
                return found
            } else {
                sleep(5)
            }
        }
        return null
    }

}
