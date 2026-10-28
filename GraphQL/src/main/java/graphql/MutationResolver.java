
*/
    public boolean updateUniteEnseignement(
            int code,
            String domaine,
            String responsable,
            int credits,
            int semestre
    ) {
        UniteEnseignement updatedUE =
                new UniteEnseignement(code, domaine, responsable, credits, semestre);

        return helper.updateUniteEnseignement(code, updatedUE);
    }

    public boolean addModule(
            String matricule,
            String nom,
            int coefficient,
            int volumeHoraire,
            entities.Module.TypeModule type
    ) {
        entities.Module module =
                new entities.Module(matricule, nom, coefficient, volumeHoraire, type);

        return moduleHelper.addModule(module);
    }
}
