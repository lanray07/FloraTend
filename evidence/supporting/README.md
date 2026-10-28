# Supporting-tool execution evidence

Execution date: 2026-10-28. Environment: Windows, local Python 3.10, workspace `D:\App Projects\FloraTend`.

- `tool-tests.log`: 12 executed reporting/website unit tests. PASS requires evidence fields/files; missing evidence is rejected. Website tests cover local noindex mode, configured-domain URLs, malformed origins, broken links and production validation failure without a domain. Temporary example.org content is a test fixture, never a deployed website.
- `website-build.log`: actual static generation of twelve English development pages. The configured production origin and Appstore URL are absent.
- `website-validation.log`: actual HTML structure, internal-link, JSON-LD and XML checks in local mode; 12 pages, zero validation errors. This does not prove deployment, visual accessibility, indexing or localisation.
- `report-generation.log`: initial 204-criterion report generation. At that execution no app observations were supplied, so no criterion was inferred as passed. Later regenerated reports may contain verified app observations from other evidence.

No browser visual inspection, real website deployment or live Appstore operation was performed by these supporting-tool tests.
