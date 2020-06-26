import React from "react";
import { Grid, GridColumn } from "@atlaskit/page";

import PageLayout from "../components/PageLayout";
import Issues from "../components/Issues";
import { Link } from "gatsby";

export default ({ location }) => {
  const { project } = location.state || { project: { name: "", issues: [] } };
  return (
    <PageLayout>
      <Grid>
        <GridColumn medium={12}>
          <div style={{ color: "#6b778c", fontWeight: "500", marginBottom: "1rem" }}>
            <Link style={{ color: "#6b778c" }} to="/dashboard">
              Project Evaluation
            </Link>{" "}
            / {project.name}
          </div>
        </GridColumn>
        <GridColumn medium={12}>
          <h2>Results</h2>
          <Issues issues={project.issues} loading={false} />
        </GridColumn>
      </Grid>
    </PageLayout>
  );
};
